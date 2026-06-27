package com.bghitech.momenta.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.repository.ProfileRepository
import com.bghitech.momenta.domain.usecase.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val avatarKey: String? = null,
    val bio: String? = null,
    val momentsCount: Int = 0,
    val streakCount: Int = 0,
    val likesCount: Int = 0,
    val recentPosts: List<Post> = emptyList(),
    val avatarOptions: List<String> = (1..25).map { index -> "avatar_%02d".format(index) },
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()
    private var loadJob: Job? = null

    init {
        loadProfile()
    }

    fun loadProfile(force: Boolean = false, showLoading: Boolean = true) {
        if (_state.value.isLoading && _state.value.username.isNotBlank() && !force) return
        if (force) loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val hasProfile = _state.value.username.isNotBlank()
            if (showLoading && !hasProfile) {
                _state.value = _state.value.copy(isLoading = true, error = null)
            } else {
                _state.value = _state.value.copy(error = null)
            }

            val cached = if (force) null else getMyProfileUseCase.getCached()
            if (cached != null) {
                mapProfile(cached)
            }

            when (val result = getMyProfileUseCase()) {
                is AppResult.Success -> {
                    mapProfile(result.data)
                    _state.value = _state.value.copy(isLoading = false)
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = if (cached == null && !hasProfile) "Не удалось загрузить профиль" else null
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

    private fun mapProfile(profile: com.bghitech.momenta.domain.model.Profile) {
        _state.value = _state.value.copy(
            username = "@${profile.username}",
            displayName = profile.displayName ?: profile.username,
            avatarUrl = profile.avatarUrl,
            avatarKey = profile.avatarKey,
            bio = profile.bio,
            momentsCount = profile.momentsCount,
            streakCount = profile.streakCount,
            likesCount = profile.likesCount,
            recentPosts = profile.recentPosts
        )
    }
}
