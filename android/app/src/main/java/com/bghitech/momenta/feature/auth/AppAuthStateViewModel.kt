package com.bghitech.momenta.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppAuthStateViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {
    val isLoggedIn = authRepository.observeAuthState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
}
