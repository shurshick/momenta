package com.bghitech.momenta.core.upload

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.bghitech.momenta.core.media.ImageCompressor
import com.bghitech.momenta.data.local.dao.UploadQueueDao
import com.bghitech.momenta.data.remote.MomentaApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadWorkerFactory @Inject constructor(
    private val uploadQueueDao: UploadQueueDao,
    private val api: MomentaApi,
    private val imageCompressor: ImageCompressor
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            UploadPostWorker::class.java.name ->
                UploadPostWorker(appContext, workerParameters, uploadQueueDao, api, imageCompressor)
            else -> null
        }
    }
}
