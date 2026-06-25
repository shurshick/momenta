package com.bghitech.momenta.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Challenge
import com.bghitech.momenta.domain.usecase.GetTodayChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayUiState(
    val isLoading: Boolean = true,
    val challenge: Challenge? = null,
    val userPostedToday: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayChallengeUseCase: GetTodayChallengeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TodayUiState())
    val state = _state.asStateFlow()

    init {
        loadChallenge()
    }

    fun loadChallenge() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val cached = getTodayChallengeUseCase.getCached()
            if (cached != null) {
                _state.value = _state.value.copy(challenge = cached)
            }

            when (val result = getTodayChallengeUseCase()) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        challenge = result.data,
                        userPostedToday = result.data.userPosted,
                        isOffline = false
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isOffline = cached == null,
                        error = if (cached == null) "Не удалось загрузить задание" else null
                    )
                }
            }
        }
    }
}
