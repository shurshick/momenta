package com.bghitech.momenta.core.upload

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bghitech.momenta.core.media.ImageCompressor
import com.bghitech.momenta.data.local.dao.UploadQueueDao
import com.bghitech.momenta.data.remote.MomentaApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@HiltWorker
class UploadPostWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val uploadQueueDao: UploadQueueDao,
    private val api: MomentaApi,
    private val imageCompressor: ImageCompressor
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val localId = inputData.getString("local_id") ?: return Result.failure()
        val entity = uploadQueueDao.getByStatus("pending").firstOrNull { it.localId == localId }
            ?: uploadQueueDao.getByStatus("uploading").firstOrNull { it.localId == localId }
            ?: return Result.failure()

        return try {
            uploadQueueDao.markFailed(localId, "uploading")

            val file = File(entity.filePath)
            if (!file.exists()) {
                uploadQueueDao.markFailed(localId, "failed")
                return Result.failure()
            }

            val compressed = imageCompressor.compressForUpload(file)

            val challengeIdPart = entity.challengeId.toRequestBody("text/plain".toMediaTypeOrNull())
            val captionPart = entity.caption?.toRequestBody("text/plain".toMediaTypeOrNull())
            val mediaPart = MultipartBody.Part.createFormData(
                "media", compressed.name,
                compressed.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )

            api.uploadPost(challengeIdPart, captionPart, null, null, mediaPart)
            uploadQueueDao.markFailed(localId, "uploaded")
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                uploadQueueDao.markFailed(localId, "pending")
                Result.retry()
            } else {
                uploadQueueDao.markFailed(localId, "failed")
                Result.failure()
            }
        }
    }
}
