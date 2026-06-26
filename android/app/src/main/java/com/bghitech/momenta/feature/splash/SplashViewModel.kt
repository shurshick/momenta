package com.bghitech.momenta.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.domain.usecase.CheckAuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkAuthUseCase: CheckAuthUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val loggedIn = checkAuthUseCase()
            _state.value = SplashState(isLoading = false, isLoggedIn = loggedIn)
        }
    }
}
