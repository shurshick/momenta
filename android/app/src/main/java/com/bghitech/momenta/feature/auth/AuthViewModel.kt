package com.bghitech.momenta.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.usecase.LoginUseCase
import com.bghitech.momenta.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoginMode: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun toggleMode() {
        _state.value = _state.value.copy(isLoginMode = !_state.value.isLoginMode, error = null)
    }

    fun login(usernameOrEmail: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = loginUseCase(usernameOrEmail, password)) {
                is AppResult.Success -> onSuccess()
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = when (result.error) {
                            AppError.Network -> "Нет подключения к серверу"
                            AppError.Unauthorized -> "Неверный логин или пароль"
                            is AppError.Validation -> result.error.message
                            else -> "Произошла ошибка"
                        }
                    )
                }
            }
        }
    }

    fun register(username: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = registerUseCase(username, email, password)) {
                is AppResult.Success -> onSuccess()
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = when (result.error) {
                            is AppError.Validation -> result.error.message
                            else -> "Произошла ошибка при регистрации"
                        }
                    )
                }
            }
        }
    }
}
