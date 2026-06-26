package com.bghitech.momenta.feature.publish

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bghitech.momenta.core.common.AppResult
import com.bghitech.momenta.core.media.ImageCompressor
import com.bghitech.momenta.domain.model.Post
import com.bghitech.momenta.domain.usecase.PublishMomentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
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
    private val publishMomentUseCase: PublishMomentUseCase
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
            } catch (e: Exception) {
                _state.value = _state.value.copy(isCompressing = false, error = "Ошибка обработки изображения")
            }
        }
    }

    fun publish(challengeId: String, caption: String?, country: String?, city: String?) {
        val file = compressedFile ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true, error = null)
            when (val result = publishMomentUseCase(challengeId, file, caption, country, city)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        isUploading = false,
                        uploaded = true,
                        uploadedPost = result.data
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isUploading = false,
                        error = when (result.error) {
                            is com.bghitech.momenta.core.common.AppError.Validation -> result.error.message
                            is com.bghitech.momenta.core.common.AppError.Unknown -> result.error.message ?: "Ошибка публикации"
                            com.bghitech.momenta.core.common.AppError.Server -> "Ошибка сервера, попробуйте позже"
                            com.bghitech.momenta.core.common.AppError.Network -> "Нет подключения к серверу"
                            else -> "Ошибка публикации"
                        }
                    )
                }
            }
        }
    }
}
