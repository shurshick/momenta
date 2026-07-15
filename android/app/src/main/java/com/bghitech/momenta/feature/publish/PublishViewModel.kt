package com.bghitech.momenta.feature.publish

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.core.common.AppError
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.common.userMessage
import com.bghitech.momenta.core.datastore.TokenStore
import com.bghitech.momenta.core.media.ImageCompressor
import com.bghitech.momenta.core.upload.UploadManager
import com.bghitech.momenta.core.util.AppDateUtils
import com.bghitech.momenta.data.local.dao.UploadQueueDao
import com.bghitech.momenta.data.local.entity.UploadQueueEntity
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.model.Profile
import com.bghitech.momenta.domain.model.User
import com.bghitech.momenta.domain.repository.FeedRepository
import com.bghitech.momenta.domain.repository.ProfileRepository
import com.bghitech.momenta.domain.usecase.PublishMomentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class PublishUiState(
    val isCompressing: Boolean = true,
    val isUploading: Boolean = false,
    val uploaded: Boolean = false,
    val uploadedPost: Post? = null,
    val error: String? = null
)

@HiltViewModel
class PublishViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageCompressor: ImageCompressor,
    private val publishMomentUseCase: PublishMomentUseCase,
    private val feedRepository: FeedRepository,
    private val profileRepository: ProfileRepository,
    private val uploadQueueDao: UploadQueueDao,
    private val uploadManager: UploadManager,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _state = MutableStateFlow(PublishUiState())
    val state = _state.asStateFlow()

    private var compressedFile: File? = null

    fun loadImage(imagePath: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCompressing = true)
            try {
                compressedFile = imageCompressor.compressForUpload(File(imagePath))
                _state.value = _state.value.copy(isCompressing = false)
            } catch (_: Exception) {
                _state.value = _state.value.copy(
                    isCompressing = false,
                    error = "Ошибка обработки изображения"
                )
            }
        }
    }

    fun publish(challengeId: String, caption: String?, country: String?, city: String?) {
        val file = compressedFile ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true, error = null)
            val localPost = buildLocalPost(file, caption, country, city)
            feedRepository.upsertLocalPost(localPost)

            when (val result = publishMomentUseCase(challengeId, file, caption, country, city)) {
                is AppResult.Success -> {
                    val uploadedPost = localPost.copy(id = result.data.id)
                    feedRepository.replaceLocalPost(localPost.id, uploadedPost)
                    _state.value = _state.value.copy(
                        isUploading = false,
                        uploaded = true,
                        uploadedPost = uploadedPost
                    )
                }
                is AppResult.Error -> {
                    if (result.error == AppError.Network || result.error == AppError.Server) {
                        queueUpload(localPost, challengeId, file, caption, country, city)
                    } else {
                        feedRepository.removeLocalPost(localPost.id)
                        _state.value = _state.value.copy(
                            isUploading = false,
                            error = publishErrorMessage(result.error)
                        )
                    }
                }
            }
        }
    }

    private suspend fun queueUpload(
        localPost: Post,
        challengeId: String,
        file: File,
        caption: String?,
        country: String?,
        city: String?
    ) {
        val accountId = tokenStore.getUserIdSync()?.takeIf { it.isNotBlank() }
        if (accountId == null) {
            feedRepository.removeLocalPost(localPost.id)
            _state.value = _state.value.copy(isUploading = false, error = "Нужен вход в аккаунт")
            return
        }
        try {
            uploadQueueDao.insert(
                UploadQueueEntity(
                    localId = localPost.id,
                    accountId = accountId,
                    challengeId = challengeId,
                    challengeDate = localPost.challengeDate,
                    filePath = file.absolutePath,
                    caption = caption,
                    country = country,
                    city = city,
                    mediaType = localPost.mediaType
                )
            )
            uploadManager.enqueueUpload(accountId, localPost.id)
            _state.value = _state.value.copy(
                isUploading = false,
                uploaded = true,
                uploadedPost = null
            )
        } catch (_: Exception) {
            feedRepository.removeLocalPost(localPost.id)
            _state.value = _state.value.copy(
                isUploading = false,
                error = "Не удалось сохранить публикацию в очередь"
            )
        }
    }

    private suspend fun buildLocalPost(file: File, caption: String?, country: String?, city: String?): Post {
        val profile = profileRepository.getCachedProfile() ?: when (val result = profileRepository.getMyProfile()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> null
        }
        return Post(
            id = "local-${System.currentTimeMillis()}",
            user = profile.toUser(),
            mediaType = "image",
            previewUrl = "file://${file.absolutePath}",
            thumbUrl = null,
            caption = caption,
            country = country,
            city = city,
            likesCount = 0,
            commentsCount = 0,
            viewsCount = 0,
            challengeDate = AppDateUtils.todayKey(),
            createdAt = nowIsoUtc(),
            isLiked = false,
            isMine = true,
            canDelete = false,
            syncState = "pending"
        )
    }

    private fun publishErrorMessage(error: AppError): String {
        return when (error) {
            is AppError.Conflict -> error.message.ifBlank { "Вы уже опубликовали момент сегодня" }
            else -> error.userMessage("Ошибка публикации")
        }
    }

    private fun Profile?.toUser(): User {
        return if (this == null) {
            User(id = "", username = "you", displayName = "Вы", avatarUrl = null, avatarKey = null, email = null)
        } else {
            User(id = id, username = username, displayName = displayName, avatarUrl = avatarUrl, avatarKey = avatarKey, email = null)
        }
    }

    private fun nowIsoUtc(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date())
    }
}
