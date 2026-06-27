package com.bghitech.momenta.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Comment
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.repository.FeedRepository
import com.bghitech.momenta.domain.repository.PostRepository
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
    val error: String? = null,
    val commentsPost: Post? = null,
    val comments: List<Comment> = emptyList(),
    val isCommentsLoading: Boolean = false,
    val commentsError: String? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getTodayFeedUseCase: GetTodayFeedUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository
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

    fun refresh() = loadFeed()

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
            val oldItems = _state.value.items
            _state.value = _state.value.copy(
                items = _state.value.items.map {
                    if (it.id == postId) it.copy(
                        isLiked = !currentlyLiked,
                        likesCount = if (currentlyLiked) (it.likesCount - 1).coerceAtLeast(0) else it.likesCount + 1
                    ) else it
                }
            )
            if (likePostUseCase(postId, !currentlyLiked) is AppResult.Error) {
                _state.value = _state.value.copy(items = oldItems)
            }
        }
    }

    fun reportPost(postId: String) {
        viewModelScope.launch {
            postRepository.reportPost(postId, "inappropriate")
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            when (postRepository.deletePost(postId)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(items = _state.value.items.filterNot { it.id == postId })
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(error = "Не удалось удалить момент")
                }
            }
        }
    }

    fun openComments(post: Post) {
        _state.value = _state.value.copy(
            commentsPost = post,
            comments = emptyList(),
            isCommentsLoading = true,
            commentsError = null
        )
        viewModelScope.launch {
            when (val result = postRepository.getComments(post.id)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        comments = result.data,
                        isCommentsLoading = false
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isCommentsLoading = false,
                        commentsError = "Не удалось загрузить комментарии"
                    )
                }
            }
        }
    }

    fun closeComments() {
        _state.value = _state.value.copy(commentsPost = null, comments = emptyList(), commentsError = null)
    }

    fun createComment(text: String) {
        val post = _state.value.commentsPost ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            when (val result = postRepository.createComment(post.id, trimmed)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        comments = _state.value.comments + result.data,
                        items = _state.value.items.map {
                            if (it.id == post.id) it.copy(commentsCount = it.commentsCount + 1) else it
                        },
                        commentsPost = post.copy(commentsCount = post.commentsCount + 1)
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(commentsError = "Не удалось отправить комментарий")
                }
            }
        }
    }

    fun deleteComment(commentId: String) {
        val post = _state.value.commentsPost ?: return
        viewModelScope.launch {
            when (postRepository.deleteComment(post.id, commentId)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        comments = _state.value.comments.filterNot { it.id == commentId },
                        items = _state.value.items.map {
                            if (it.id == post.id) it.copy(commentsCount = (it.commentsCount - 1).coerceAtLeast(0)) else it
                        },
                        commentsPost = post.copy(commentsCount = (post.commentsCount - 1).coerceAtLeast(0))
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(commentsError = "Не удалось удалить комментарий")
                }
            }
        }
    }
}
