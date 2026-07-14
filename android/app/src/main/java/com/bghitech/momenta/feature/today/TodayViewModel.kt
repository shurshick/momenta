package com.bghitech.momenta.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.common.userMessage
import com.bghitech.momenta.core.util.AppDateUtils
import com.bghitech.momenta.domain.model.Challenge
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.repository.FeedRepository
import com.bghitech.momenta.domain.usecase.GetTodayChallengeUseCase
import com.bghitech.momenta.domain.usecase.GetTodayFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayUiState(
    val isChallengeLoading: Boolean = false,
    val challenge: Challenge? = null,
    val bestPost: Post? = null,
    val isBestMomentLoading: Boolean = false,
    val feedLoaded: Boolean = false,
    val userPostedToday: Boolean = false,
    val challengeOffline: Boolean = false,
    val bestMomentOffline: Boolean = false,
    val challengeError: String? = null,
    val bestMomentError: String? = null
) {
    val isOffline: Boolean
        get() = challengeOffline || bestMomentOffline
}

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayChallengeUseCase: GetTodayChallengeUseCase,
    private val getTodayFeedUseCase: GetTodayFeedUseCase,
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TodayUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun loadChallenge() {
        if (_state.value.isChallengeLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isChallengeLoading = true, challengeError = null)

            val cached = getTodayChallengeUseCase.getCached()
            if (cached != null && _state.value.challenge == null) {
                _state.value = _state.value.copy(
                    challenge = cached,
                    userPostedToday = cached.userPosted
                )
            }

            when (val result = getTodayChallengeUseCase()) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        isChallengeLoading = false,
                        challenge = result.data,
                        userPostedToday = result.data.userPosted,
                        challengeOffline = false,
                        challengeError = null
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isChallengeLoading = false,
                        challengeOffline = true,
                        challengeError = if (cached == null) {
                            result.error.userMessage("Не удалось загрузить задание дня")
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    fun refreshBestMoment() {
        if (_state.value.isBestMomentLoading) return
        viewModelScope.launch {
            loadBestPost()
        }
    }

    fun refresh() {
        dropExpiredDayState()
        loadChallenge()
        refreshBestMoment()
    }

    private fun dropExpiredDayState() {
        val today = AppDateUtils.todayKey()
        val current = _state.value
        val challenge = current.challenge?.takeIf { it.date == today }
        val bestPost = current.bestPost?.takeIf { it.challengeDate == today }
        if (challenge != current.challenge || bestPost != current.bestPost) {
            _state.value = current.copy(
                challenge = challenge,
                bestPost = bestPost,
                feedLoaded = false,
                userPostedToday = challenge?.userPosted ?: false,
                challengeOffline = false,
                bestMomentOffline = false,
                challengeError = null,
                bestMomentError = null
            )
        }
    }

    private suspend fun loadBestPost() {
        _state.value = _state.value.copy(isBestMomentLoading = true, bestMomentError = null)
        val cached = getTodayFeedUseCase.getCached()
        if (cached.isNotEmpty() && _state.value.bestPost == null) {
            _state.value = _state.value.copy(bestPost = cached.bestMoment(), feedLoaded = true)
        }

        when (val result = feedRepository.getBestMoment()) {
            is AppResult.Success -> {
                val currentBestPost = _state.value.bestPost
                val fallbackPost = result.data ?: currentBestPost ?: firstFeedPost() ?: cached.bestMoment()
                _state.value = _state.value.copy(
                    bestPost = fallbackPost,
                    feedLoaded = true,
                    isBestMomentLoading = false,
                    bestMomentOffline = false,
                    bestMomentError = null
                )
            }
            is AppResult.Error -> {
                val fallbackPost = _state.value.bestPost ?: firstFeedPost() ?: cached.bestMoment()
                _state.value = _state.value.copy(
                    bestPost = fallbackPost,
                    feedLoaded = fallbackPost != null || cached.isNotEmpty(),
                    isBestMomentLoading = false,
                    bestMomentOffline = true,
                    bestMomentError = if (fallbackPost == null) {
                        "Лучший момент дня пока не найден"
                    } else {
                        null
                    }
                )
            }
        }
    }

    private suspend fun firstFeedPost(): Post? =
        when (val result = feedRepository.getTodayFeed(cursor = null, limit = 10)) {
            is AppResult.Success -> result.data.bestMoment() ?: result.data.firstOrNull()
            is AppResult.Error -> null
        }

    private fun List<Post>.bestMoment(): Post? =
        filter { it.previewUrl.isNotBlank() || !it.thumbUrl.isNullOrBlank() }
            .maxWithOrNull(compareBy<Post> { it.likesCount }.thenBy { it.createdAt })
}
