package com.bghitech.momenta.core.upload

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun enqueueUpload(localId: String) {
        val workData = workDataOf("local_id" to localId)

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
            .addTag("upload_$localId")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "upload_$localId",
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    fun cancelUpload(localId: String) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("upload_$localId")
    }
}
