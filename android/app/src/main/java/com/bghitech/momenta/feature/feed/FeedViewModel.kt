package com.bghitech.momenta.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.util.AppDateUtils
import com.bghitech.momenta.domain.model.Comment
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.model.User
import com.bghitech.momenta.domain.repository.FeedRepository
import com.bghitech.momenta.domain.repository.PostRepository
import com.bghitech.momenta.domain.repository.ProfileRepository
import com.bghitech.momenta.domain.usecase.GetTodayFeedUseCase
import com.bghitech.momenta.domain.usecase.LikePostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    val commentsError: String? = null,
    val suggestedUsers: List<User> = emptyList()
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getTodayFeedUseCase: GetTodayFeedUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedUiState())
    val state = _state.asStateFlow()
    private var syncJob: Job? = null
    private var feedObservationJob: Job? = null
    private var observedDate: String? = null

    init {
        observeFeedStore()
        loadFeed()
        loadUserSuggestions()
    }

    fun loadFeed(force: Boolean = false) {
        if (_state.value.isLoading && !force) return
        if (force) syncJob?.cancel()
        syncJob = viewModelScope.launch { syncFeed() }
    }

    fun refresh() {
        observeFeedStore()
        loadFeed(force = true)
    }

    fun loadMore() {
        if (_state.value.isLoadingMore) return
        viewModelScope.launch {
            val cursor = getTodayFeedUseCase.getNextCursor() ?: return@launch
            _state.value = _state.value.copy(isLoadingMore = true)
            when (getTodayFeedUseCase(cursor = cursor)) {
                is AppResult.Success -> _state.value = _state.value.copy(isLoadingMore = false)
                is AppResult.Error -> _state.value = _state.value.copy(isLoadingMore = false)
            }
        }
    }

    fun toggleLike(postId: String, currentlyLiked: Boolean) {
        viewModelScope.launch {
            val oldPost = _state.value.items.firstOrNull { it.id == postId } ?: return@launch
            val likesDelta = if (currentlyLiked) -1 else 1
            val optimisticPost = oldPost.copy(
                isLiked = !currentlyLiked,
                likesCount = (oldPost.likesCount + likesDelta).coerceAtLeast(0)
            )
            feedRepository.updateCachedPost(optimisticPost)

            if (likePostUseCase(postId, !currentlyLiked) is AppResult.Error) {
                feedRepository.updateCachedPost(oldPost)
            } else if (oldPost.isMine) {
                adjustCachedProfileLikes(likesDelta)
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
            val deletedPost = _state.value.items.firstOrNull { it.id == postId }
            when (postRepository.deletePost(postId)) {
                is AppResult.Success -> {
                    feedRepository.removeLocalPost(postId)
                    if (deletedPost?.isMine == true) {
                        removePostFromCachedProfile(deletedPost)
                    }
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
                    val updatedPost = post.copy(commentsCount = post.commentsCount + 1)
                    feedRepository.updateCachedPost(updatedPost)
                    _state.value = _state.value.copy(
                        comments = _state.value.comments + result.data,
                        commentsPost = updatedPost
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
                    val updatedPost = post.copy(commentsCount = (post.commentsCount - 1).coerceAtLeast(0))
                    feedRepository.updateCachedPost(updatedPost)
                    _state.value = _state.value.copy(
                        comments = _state.value.comments.filterNot { it.id == commentId },
                        commentsPost = updatedPost
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(commentsError = "Не удалось удалить комментарий")
                }
            }
        }
    }

    private fun observeFeedStore() {
        val today = AppDateUtils.todayKey()
        if (observedDate == today && feedObservationJob?.isActive == true) return
        observedDate = today
        feedObservationJob?.cancel()
        _state.value = _state.value.copy(
            items = emptyList(),
            isOffline = false,
            error = null
        )
        feedObservationJob = viewModelScope.launch {
            feedRepository.observeTodayFeed().collect { posts ->
                _state.value = _state.value.copy(
                    items = posts,
                    suggestedUsers = posts.suggestedUsers().ifEmpty { _state.value.suggestedUsers },
                    error = if (posts.isNotEmpty()) null else _state.value.error
                )
            }
        }
    }

    private suspend fun syncFeed() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        when (getTodayFeedUseCase()) {
            is AppResult.Success -> {
                _state.value = _state.value.copy(isLoading = false, isOffline = false, error = null)
            }
            is AppResult.Error -> {
                val hasCached = _state.value.items.isNotEmpty()
                _state.value = _state.value.copy(
                    isLoading = false,
                    isOffline = true,
                    error = if (hasCached) null else "Не удалось загрузить ленту"
                )
            }
        }
    }

    private fun loadUserSuggestions() {
        viewModelScope.launch {
            when (val result = feedRepository.getUserSuggestions()) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        suggestedUsers = result.data.distinctBy { it.username }.take(20)
                    )
                }
                is AppResult.Error -> Unit
            }
        }
    }

    private fun List<Post>.suggestedUsers(): List<User> =
        groupBy { it.user.username }
            .values
            .sortedWith(
                compareByDescending<List<Post>> { it.size }
                    .thenByDescending { posts -> posts.maxOfOrNull { it.createdAt }.orEmpty() }
            )
            .map { it.first().user }
            .filter { it.username.isNotBlank() }
            .take(20)

    private suspend fun adjustCachedProfileLikes(delta: Int) {
        val cachedProfile = profileRepository.getCachedProfile() ?: return
        profileRepository.cacheProfile(
            cachedProfile.copy(likesCount = (cachedProfile.likesCount + delta).coerceAtLeast(0))
        )
    }

    private suspend fun removePostFromCachedProfile(post: Post) {
        val cachedProfile = profileRepository.getCachedProfile() ?: return
        profileRepository.cacheProfile(
            cachedProfile.copy(
                momentsCount = (cachedProfile.momentsCount - 1).coerceAtLeast(0),
                likesCount = (cachedProfile.likesCount - post.likesCount).coerceAtLeast(0),
                recentPosts = cachedProfile.recentPosts.filterNot { it.id == post.id }
            )
        )
    }
}
