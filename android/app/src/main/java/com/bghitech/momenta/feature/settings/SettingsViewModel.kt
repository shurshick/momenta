package com.bghitech.momenta.feature.settings

import androidx.lifecycle.ViewModel
import com.bghitech.momenta.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    suspend fun logout() {
        logoutUseCase()
    }
}
