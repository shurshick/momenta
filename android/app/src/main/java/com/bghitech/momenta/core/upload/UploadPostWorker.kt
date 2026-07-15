package com.bghitech.momenta.core.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.data.local.dao.PostDao
import com.bghitech.momenta.data.local.dao.UploadQueueDao
import com.bghitech.momenta.data.remote.MomentaApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class UploadPostWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val uploadQueueDao: UploadQueueDao,
    private val postDao: PostDao,
    private val api: MomentaApi,
    private val tokenStore: TokenStore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val accountId = inputData.getString("account_id") ?: return Result.failure()
        val localId = inputData.getString("local_id") ?: return Result.failure()
        val entity = uploadQueueDao.getById(localId) ?: return Result.success()
        if (entity.accountId != accountId || tokenStore.getUserIdSync() != accountId) {
            return Result.failure()
        }

        return try {
            uploadQueueDao.updateStatus(localId, "uploading")

            val file = File(entity.filePath)
            if (!file.exists()) {
                postDao.deleteById(accountId, localId)
                uploadQueueDao.deleteById(localId)
                return Result.failure()
            }

            val challengeIdPart = entity.challengeId.toRequestBody("text/plain".toMediaTypeOrNull())
            val textType = "text/plain".toMediaTypeOrNull()
            val captionPart = entity.caption?.toRequestBody(textType)
            val countryPart = entity.country?.toRequestBody(textType)
            val cityPart = entity.city?.toRequestBody(textType)
            val mediaPart = MultipartBody.Part.createFormData(
                "media", file.name,
                file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )

            val response = api.uploadPost(challengeIdPart, captionPart, countryPart, cityPart, mediaPart)
            postDao.getById(accountId, localId)?.let { localPost ->
                postDao.replacePostId(
                    accountId,
                    localId,
                    localPost.copy(
                        id = response.id,
                        syncState = "uploaded",
                        cachedAt = System.currentTimeMillis()
                    )
                )
            }
            uploadQueueDao.deleteById(localId)
            Result.success()
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 409) {
                postDao.deleteById(accountId, localId)
                uploadQueueDao.deleteById(localId)
                return Result.success()
            }
            if (runAttemptCount < 3) {
                uploadQueueDao.updateStatus(localId, "pending")
                Result.retry()
            } else {
                uploadQueueDao.markFailed(localId, "failed")
                Result.failure()
            }
        }
    }
}
