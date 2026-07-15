package com.bghitech.momenta.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.repository.ProfileRepository
import com.bghitech.momenta.domain.repository.FeedRepository
import com.bghitech.momenta.domain.repository.PostRepository
import com.bghitech.momenta.domain.usecase.GetMyProfileUseCase
import com.bghitech.momenta.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val avatarKey: String? = null,
    val bio: String? = null,
    val momentsCount: Int = 0,
    val streakCount: Int = 0,
    val likesCount: Int = 0,
    val recentPosts: List<Post> = emptyList(),
    val ownPosts: List<Post> = emptyList(),
    val ownPostsNextCursor: String? = null,
    val isOwnPostsLoading: Boolean = false,
    val bookmarkedPosts: List<Post> = emptyList(),
    val bookmarksNextCursor: String? = null,
    val isBookmarksLoading: Boolean = false,
    val isOffline: Boolean = false,
    val avatarOptions: List<String> = (1..40).map { index -> "avatar_%02d".format(index) },
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val profileRepository: ProfileRepository,
    private val feedRepository: FeedRepository,
    private val postRepository: PostRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()
    private var loadJob: Job? = null
    private var bookmarksJob: Job? = null
    private var ownPostsJob: Job? = null
    private var lastProfileAttemptAt: Long = 0L
    private var lastBookmarksAttemptAt: Long = 0L

    init {
        loadProfile()
        observeBookmarks()
        loadBookmarks()
    }

    fun loadBookmarks(force: Boolean = false, loadMore: Boolean = false) {
        if (bookmarksJob?.isActive == true && !force) return
        if (force) bookmarksJob?.cancel()
        val cursor = if (loadMore) _state.value.bookmarksNextCursor ?: return else null
        bookmarksJob = viewModelScope.launch {
            lastBookmarksAttemptAt = monotonicTimeMillis()
            _state.value = _state.value.copy(isBookmarksLoading = true)
            when (val result = feedRepository.syncBookmarks(cursor)) {
                is AppResult.Success -> _state.value = _state.value.copy(
                    isBookmarksLoading = false,
                    bookmarksNextCursor = result.data
                )
                is AppResult.Error -> _state.value = _state.value.copy(isBookmarksLoading = false)
            }
        }
    }

    fun loadMoreBookmarks() = loadBookmarks(loadMore = true)

    fun loadOwnPosts(force: Boolean = false, loadMore: Boolean = false) {
        val userId = _state.value.userId.takeIf { it.isNotBlank() } ?: return
        if (ownPostsJob?.isActive == true && !force) return
        if (force) ownPostsJob?.cancel()
        val cursor = if (loadMore) _state.value.ownPostsNextCursor ?: return else null
        ownPostsJob = viewModelScope.launch {
            _state.value = _state.value.copy(isOwnPostsLoading = true)
            when (val result = profileRepository.getUserPosts(userId, cursor)) {
                is AppResult.Success -> {
                    val posts = if (cursor == null) {
                        result.data.first
                    } else {
                        (_state.value.ownPosts + result.data.first).distinctBy { it.id }
                    }
                    _state.value = _state.value.copy(
                        ownPosts = posts,
                        ownPostsNextCursor = result.data.second,
                        isOwnPostsLoading = false
                    )
                }
                is AppResult.Error -> _state.value = _state.value.copy(isOwnPostsLoading = false)
            }
        }
    }

    fun loadMoreOwnPosts() = loadOwnPosts(loadMore = true)

    fun onScreenResumed() {
        val now = monotonicTimeMillis()
        if (loadJob?.isActive != true && now - lastProfileAttemptAt >= RESUME_SYNC_INTERVAL_MS) {
            loadProfile(force = true, showLoading = false)
        }
        if (bookmarksJob?.isActive != true && now - lastBookmarksAttemptAt >= RESUME_SYNC_INTERVAL_MS) {
            loadBookmarks(force = true)
        }
    }

    fun removeBookmark(post: Post) {
        viewModelScope.launch {
            val optimistic = post.copy(isBookmarked = false, bookmarkedAt = null)
            feedRepository.updateCachedPost(optimistic)
            if (postRepository.unbookmarkPost(post.id) is AppResult.Error) {
                feedRepository.updateCachedPost(post)
                _state.value = _state.value.copy(error = "Не удалось удалить из избранного")
            }
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            feedRepository.observeBookmarks().collect { posts ->
                _state.value = _state.value.copy(bookmarkedPosts = posts)
            }
        }
    }

    fun loadProfile(force: Boolean = false, showLoading: Boolean = true) {
        if (loadJob?.isActive == true && !force) return
        if (force) loadJob?.cancel()
        loadJob = viewModelScope.launch {
            lastProfileAttemptAt = monotonicTimeMillis()
            val cached = getMyProfileUseCase.getCached()
            if (cached != null && _state.value.username.isBlank()) {
                mapProfile(cached)
                _state.value = _state.value.copy(isLoading = false)
            }

            val hasProfile = _state.value.username.isNotBlank()
            if (showLoading && !hasProfile) {
                _state.value = _state.value.copy(isLoading = true, error = null)
            } else {
                _state.value = _state.value.copy(error = null)
            }

            when (val result = getMyProfileUseCase()) {
                is AppResult.Success -> {
                    mapProfile(result.data)
                    loadOwnPosts(force = true)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isOffline = false,
                        error = null
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isOffline = true,
                        error = if (cached == null && !hasProfile) {
                            "Профиль недоступен без интернета"
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    fun updateProfile(displayName: String, bio: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            when (val result = profileRepository.updateProfile(displayName.ifBlank { null }, bio?.ifBlank { null })) {
                is AppResult.Success -> {
                    mapProfile(result.data)
                    _state.value = _state.value.copy(isSaving = false)
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = "Не удалось сохранить профиль"
                    )
                }
            }

            when (val avatars = profileRepository.getAvatars()) {
                is AppResult.Success -> _state.value = _state.value.copy(avatarOptions = avatars.data)
                is AppResult.Error -> Unit
            }
        }
    }

    fun updateAvatar(avatarKey: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            when (val result = profileRepository.updateAvatar(avatarKey)) {
                is AppResult.Success -> {
                    mapProfile(result.data)
                    _state.value = _state.value.copy(isSaving = false)
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = "Не удалось сохранить аватар"
                    )
                }
            }
        }
    }

    suspend fun logout() {
        logoutUseCase()
    }

    private fun mapProfile(profile: com.bghitech.momenta.domain.model.Profile) {
        val existingOwnPosts = _state.value.ownPosts.takeIf { _state.value.userId == profile.id && it.isNotEmpty() }
        _state.value = _state.value.copy(
            userId = profile.id,
            username = "@${profile.username}",
            displayName = profile.displayName ?: profile.username,
            avatarUrl = profile.avatarUrl,
            avatarKey = profile.avatarKey,
            bio = profile.bio,
            momentsCount = profile.momentsCount,
            streakCount = profile.streakCount,
            likesCount = profile.likesCount,
            recentPosts = profile.recentPosts,
            ownPosts = existingOwnPosts ?: profile.recentPosts
        )
    }

    private companion object {
        const val RESUME_SYNC_INTERVAL_MS = 60_000L

        fun monotonicTimeMillis(): Long = System.nanoTime() / 1_000_000L
    }
}
