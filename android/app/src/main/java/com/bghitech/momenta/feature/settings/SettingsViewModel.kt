package com.bghitech.momenta.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.BuildConfig
import com.bghitech.momenta.domain.repository.SettingsRepository
import com.bghitech.momenta.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.net.URL
import javax.inject.Inject

data class SettingsUiState(
    val savedServerUrl: String = BuildConfig.DEFAULT_SERVER_URL,
    val serverUrlInput: String = BuildConfig.DEFAULT_SERVER_URL,
    val loggingEnabled: Boolean = BuildConfig.LOGGING_ENABLED,
    val isCheckingConnection: Boolean = false,
    val connectionMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getServerUrl().collectLatest { serverUrl ->
                _state.update {
                    it.copy(
                        savedServerUrl = serverUrl,
                        serverUrlInput = if (it.serverUrlInput.isBlank()) serverUrl else it.serverUrlInput
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.getLoggingEnabled().collectLatest { enabled ->
                _state.update { it.copy(loggingEnabled = enabled) }
            }
        }
    }

    fun onServerUrlChange(value: String) {
        _state.update { it.copy(serverUrlInput = value, connectionMessage = null) }
    }

    fun setLoggingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLoggingEnabled(enabled)
        }
    }

    suspend fun logout() {
        logoutUseCase()
    }

    fun saveAndCheckServer() {
        val normalized = normalizeServerUrl(_state.value.serverUrlInput)
        if (normalized == null) {
            _state.update { it.copy(connectionMessage = "Введите корректный URL сервера.") }
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    serverUrlInput = normalized,
                    isCheckingConnection = true,
                    connectionMessage = "Проверяем соединение..."
                )
            }
            val message = checkServerHealth(normalized)
            settingsRepository.setServerUrl(normalized)
            _state.update {
                it.copy(
                    savedServerUrl = normalized,
                    isCheckingConnection = false,
                    connectionMessage = message
                )
            }
        }
    }

    private fun normalizeServerUrl(value: String): String? {
        val trimmed = value.trim().trimEnd('/')
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) return null
        return trimmed.takeIf { it.length > "https://x".length }
    }

    private suspend fun checkServerHealth(baseUrl: String): String = withContext(Dispatchers.IO) {
        runCatching {
            URL("$baseUrl/health").openConnection().apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
            }.getInputStream().use { it.readBytes() }
            "Соединение с сервером работает."
        }.getOrElse {
            "Не удалось подключиться к серверу. URL сохранен, но проверь адрес и сеть."
        }
    }
}
