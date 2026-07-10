package com.bghitech.momenta

import android.app.Application
import androidx.work.Configuration
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.core.upload.UploadWorkerFactory
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

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { tokenStore.warmUp() }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
