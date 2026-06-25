package com.bghitech.momenta

import android.app.Application
import androidx.work.Configuration
import com.bghitech.momenta.core.upload.UploadWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MomentaApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: UploadWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
