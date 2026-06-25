package com.bghitech.momenta.feature.camera

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CameraUiState(
    val flashMode: Boolean = false,
    val isFrontCamera: Boolean = false,
    val isCapturing: Boolean = false
)

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CameraUiState())
    val state = _state.asStateFlow()

    fun toggleFlash() {
        _state.value = _state.value.copy(flashMode = !_state.value.flashMode)
    }

    fun switchCamera() {
        _state.value = _state.value.copy(isFrontCamera = !_state.value.isFrontCamera)
    }

    fun setCapturing(capturing: Boolean) {
        _state.value = _state.value.copy(isCapturing = capturing)
    }
}
