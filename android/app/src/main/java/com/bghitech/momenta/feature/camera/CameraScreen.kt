package com.bghitech.momenta.feature.camera

import android.Manifest
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bghitech.momenta.core.design.*
import com.bghitech.momenta.core.permissions.CameraPermissionContent
import com.google.accompanist.permissions.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onImageCaptured: (String) -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (!cameraPermissionState.status.isGranted) {
        CameraPermissionContent(
            onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
        )
        return
    }

    CameraContent(
        state = state,
        onBack = onBack,
        onImageCaptured = onImageCaptured,
        onToggleFlash = { viewModel.toggleFlash() }
    )
}

@Composable
private fun CameraContent(
    state: CameraUiState,
    onBack: () -> Unit,
    onImageCaptured: (String) -> Unit,
    onToggleFlash: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val provider = ProcessCameraProvider.getInstance(ctx)
                provider.addListener({
                    val cameraProvider = provider.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setFlashMode(
                            if (state.flashMode) ImageCapture.FLASH_MODE_ON
                            else ImageCapture.FLASH_MODE_OFF
                        )
                        .build()

                    val cameraSelector = if (state.isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageCapture
                        )
                    } catch (_: Exception) { }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, "Закрыть", tint = MomentaText)
            }
            IconButton(onClick = onToggleFlash) {
                Icon(
                    if (state.flashMode) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    "Вспышка",
                    tint = if (state.flashMode) MomentaWarm else MomentaText
                )
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gallery + Effects row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Галерея",
                    color = MomentaText,
                    fontSize = 12.sp
                )

                IconButton(
                    onClick = {
                        val photoFile = File(
                            context.cacheDir,
                            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
                        )
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                        imageCapture?.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    onImageCaptured(photoFile.absolutePath)
                                }
                                override fun onError(exception: ImageCaptureException) { }
                            }
                        )
                    },
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MomentaGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MomentaBackground)
                        )
                    }
                }

                Text(
                    text = "Эффекты",
                    color = MomentaText,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mode selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                listOf("Фото", "Видео", "История").forEach { mode ->
                    Text(
                        text = mode,
                        color = if (mode == "Фото") MomentaGreen else MomentaTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (mode == "Фото") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}
