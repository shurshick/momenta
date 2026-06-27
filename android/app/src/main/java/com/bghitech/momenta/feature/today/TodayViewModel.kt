package com.bghitech.momenta.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Challenge
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.repository.FeedRepository
import com.bghitech.momenta.domain.usecase.GetTodayFeedUseCase
import com.bghitech.momenta.domain.usecase.GetTodayChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayUiState(
    val isLoading: Boolean = true,
    val challenge: Challenge? = null,
    val bestPost: Post? = null,
    val isBestMomentLoading: Boolean = false,
    val feedLoaded: Boolean = false,
    val userPostedToday: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayChallengeUseCase: GetTodayChallengeUseCase,
    private val getTodayFeedUseCase: GetTodayFeedUseCase,
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TodayUiState())
    val state = _state.asStateFlow()

    init {
        loadChallenge()
        refreshBestMoment()
    }

    fun loadChallenge() {
        if (_state.value.isLoading && _state.value.challenge != null) return
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

            loadBestPost()
        }
    }

    fun refreshBestMoment() {
        if (_state.value.isBestMomentLoading) return
        viewModelScope.launch {
            loadBestPost()
        }
    }

    private suspend fun loadBestPost() {
        _state.value = _state.value.copy(isBestMomentLoading = true)
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
                    isBestMomentLoading = false
                )
            }
            is AppResult.Error -> {
                val fallbackPost = _state.value.bestPost ?: firstFeedPost() ?: cached.bestMoment()
                _state.value = _state.value.copy(
                    bestPost = fallbackPost,
                    feedLoaded = fallbackPost != null || cached.isNotEmpty(),
                    isBestMomentLoading = false
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
