package com.bghitech.momenta.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.core.common.AppResult
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
import kotlinx.coroutines.delay
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
    val suggestedUsers: List<User> = emptyList(),
    val scrollToTopSignal: Int = 0
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
    private var loadJob: Job? = null
    private var publishRefreshJob: Job? = null
    private var userScrolledAfterPublish = false

    init {
        loadFeed()
        loadUserSuggestions()
    }

    fun loadFeed(showCached: Boolean = true, scrollToTop: Boolean = false, force: Boolean = false) {
        if (_state.value.isLoading && !force) return
        if (force) loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadFeedNow(showCached = showCached, scrollToTop = scrollToTop)
        }
    }

    fun refresh() {
        if (_state.value.isLoading) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadFeedNow(
                showCached = false,
                scrollToTop = false,
                keepExistingWhileLoading = true
            )
        }
    }

    fun refreshAfterPublish() {
        publishRefreshJob?.cancel()
        userScrolledAfterPublish = false
        loadJob?.cancel()
        publishRefreshJob = viewModelScope.launch {
            val previousTopId = _state.value.items.firstOrNull()?.id
            loadFeedNow(
                showCached = false,
                scrollToTop = false,
                keepExistingWhileLoading = true,
                scrollToTopWhenChangedFromId = previousTopId
            )
            for (pauseMs in listOf(900L, 1600L, 2600L, 4200L)) {
                if (userScrolledAfterPublish) break
                val currentTopId = _state.value.items.firstOrNull()?.id
                if (currentTopId != null && currentTopId != previousTopId) break
                delay(pauseMs)
                if (userScrolledAfterPublish) break
                loadFeedNow(
                    showCached = false,
                    scrollToTop = false,
                    keepExistingWhileLoading = true,
                    scrollToTopWhenChangedFromId = previousTopId
                )
            }
        }
    }

    fun onUserScrolledAfterPublish() {
        if (publishRefreshJob?.isActive == true) {
            userScrolledAfterPublish = true
        }
    }

    private suspend fun loadFeedNow(
        showCached: Boolean,
        scrollToTop: Boolean,
        keepExistingWhileLoading: Boolean = false,
        scrollToTopWhenChangedFromId: String? = null
    ) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        val cached = getTodayFeedUseCase.getCached()
        if (showCached && cached.isNotEmpty() && !keepExistingWhileLoading) {
            _state.value = _state.value.copy(
                items = cached,
                suggestedUsers = _state.value.suggestedUsers.ifEmpty { cached.suggestedUsers() }
            )
        }

        when (val result = getTodayFeedUseCase()) {
            is AppResult.Success -> {
                val shouldScrollToTop = scrollToTop ||
                        (scrollToTopWhenChangedFromId != null &&
                                result.data.firstOrNull()?.id != null &&
                                result.data.firstOrNull()?.id != scrollToTopWhenChangedFromId)
                _state.value = _state.value.copy(
                    isLoading = false,
                    items = result.data,
                    suggestedUsers = _state.value.suggestedUsers.ifEmpty { result.data.suggestedUsers() },
                    isOffline = false,
                    scrollToTopSignal = if (shouldScrollToTop) _state.value.scrollToTopSignal + 1 else _state.value.scrollToTopSignal
                )
            }
            is AppResult.Error -> {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isOffline = cached.isEmpty(),
                    error = if (cached.isEmpty()) "Не удалось загрузить ленту" else null,
                    scrollToTopSignal = if (scrollToTop) _state.value.scrollToTopSignal + 1 else _state.value.scrollToTopSignal
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
            val targetPost = oldItems.firstOrNull { it.id == postId }
            val likesDelta = if (currentlyLiked) -1 else 1
            _state.value = _state.value.copy(
                items = _state.value.items.map {
                    if (it.id == postId) it.copy(
                        isLiked = !currentlyLiked,
                        likesCount = (it.likesCount + likesDelta).coerceAtLeast(0)
                    ) else it
                }
            )
            if (likePostUseCase(postId, !currentlyLiked) is AppResult.Error) {
                _state.value = _state.value.copy(items = oldItems)
            } else if (targetPost?.isMine == true) {
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
                    val newItems = _state.value.items.filterNot { it.id == postId }
                    _state.value = _state.value.copy(
                        items = newItems,
                        suggestedUsers = _state.value.suggestedUsers.ifEmpty { newItems.suggestedUsers() }
                    )
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

    private fun List<Post>.suggestedUsers(): List<User> =
        map { it.user }
            .filter { it.username.isNotBlank() }
            .distinctBy { it.username }
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
