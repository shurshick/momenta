package com.bghitech.momenta.feature.updates

import androidx.lifecycle.ViewModel
import com.bghitech.momenta.data.remote.MomentaApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val api: MomentaApi
) : ViewModel() {
    suspend fun checkLatestAppRelease(): AppUpdateInfo =
        com.bghitech.momenta.feature.updates.checkLatestAppRelease(api)
}
