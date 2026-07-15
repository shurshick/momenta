package com.bghitech.momenta

import android.app.Application
import androidx.work.Configuration
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.core.upload.UploadWorkerFactory
import com.bghitech.momenta.core.upload.UploadManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MomentaApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: UploadWorkerFactory

    @Inject
    lateinit var tokenStore: TokenStore

    @Inject
    lateinit var uploadManager: UploadManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            tokenStore.warmUp()
            tokenStore.getUserIdSync()?.takeIf { it.isNotBlank() }?.let {
                uploadManager.resumePendingUploads(it)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
