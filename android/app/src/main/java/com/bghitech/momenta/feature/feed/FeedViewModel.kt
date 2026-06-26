package com.bghitech.momenta.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.repository.FeedRepository
import com.bghitech.momenta.domain.usecase.GetTodayFeedUseCase
import com.bghitech.momenta.domain.usecase.LikePostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val isLoading: Boolean = false,
    val items: List<Post> = emptyList(),
    val isLoadingMore: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getTodayFeedUseCase: GetTodayFeedUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedUiState())
    val state = _state.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val cached = getTodayFeedUseCase.getCached()
            if (cached.isNotEmpty()) {
                _state.value = _state.value.copy(items = cached)
            }

            when (val result = getTodayFeedUseCase()) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        items = result.data,
                        isOffline = false
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isOffline = cached.isEmpty(),
                        error = if (cached.isEmpty()) "Не удалось загрузить ленту" else null
                    )
                }
            }
        }
    }

    fun loadMore() {
        if (_state.value.isLoadingMore) return
        viewModelScope.launch {
            val cursor = feedRepository.getNextCursor() ?: return@launch
            _state.value = _state.value.copy(isLoadingMore = true)
            when (val result = getTodayFeedUseCase(cursor = cursor)) {
                is AppResult.Success -> {
                    val existingIds = _state.value.items.map { it.id }.toSet()
                    val newItems = result.data.filter { it.id !in existingIds }
                    _state.value = _state.value.copy(
                        isLoadingMore = false,
                        items = _state.value.items + newItems
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(isLoadingMore = false)
                }
            }
        }
    }

    fun toggleLike(postId: String, currentlyLiked: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                items = _state.value.items.map {
                    if (it.id == postId) it.copy(
                        isLiked = !currentlyLiked,
                        likesCount = if (currentlyLiked) it.likesCount - 1 else it.likesCount + 1
                    ) else it
                }
            )
            likePostUseCase(postId, !currentlyLiked)
        }
    }
}
