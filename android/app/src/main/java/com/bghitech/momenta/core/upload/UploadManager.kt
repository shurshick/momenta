package com.bghitech.momenta.core.upload

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val uploadQueueDao: com.bghitech.momenta.data.local.dao.UploadQueueDao
) {
    fun enqueueUpload(accountId: String, localId: String) {
        val workData = workDataOf("account_id" to accountId, "local_id" to localId)

        val request = OneTimeWorkRequestBuilder<UploadPostWorker>()
            .setInputData(workData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .addTag(accountTag(accountId))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                workName(accountId, localId),
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    suspend fun resumePendingUploads(accountId: String) {
        uploadQueueDao.getPendingForAccount(accountId).forEach { entity ->
            uploadQueueDao.updateStatus(entity.localId, "pending")
            enqueueUpload(accountId, entity.localId)
        }
    }

    fun cancelUploads(accountId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(accountTag(accountId))
    }

    fun cancelUpload(accountId: String, localId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(accountId, localId))
    }

    private fun workName(accountId: String, localId: String) = "upload_${accountId}_$localId"

    private fun accountTag(accountId: String) = "uploads_$accountId"
}
