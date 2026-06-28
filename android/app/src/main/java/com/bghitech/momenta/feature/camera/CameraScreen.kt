package com.bghitech.momenta.feature.camera

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bghitech.momenta.core.design.MomentaBackground
import com.bghitech.momenta.core.design.MomentaDivider
import com.bghitech.momenta.core.design.MomentaGreen
import com.bghitech.momenta.core.design.MomentaSurface
import com.bghitech.momenta.core.design.MomentaText
import com.bghitech.momenta.core.design.MomentaTextSecondary
import com.bghitech.momenta.core.design.MomentaWarm
import com.bghitech.momenta.core.media.PhotoEffect
import com.bghitech.momenta.core.media.PhotoEffectProcessor
import com.bghitech.momenta.core.permissions.CameraPermissionContent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private enum class MomentEffect(val title: String, val subtitle: String) {
    Natural("Оригинал", "Без обработки"),
    Warm("Тепло", "Мягкий вечерний тон"),
    Vivid("Живой", "Больше света и цвета"),
    Mono("Ч/Б", "Контрастный монохром")
}

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
    var selectedEffect by remember { mutableStateOf(PhotoEffect.Natural) }
    var showEffects by remember { mutableStateOf(false) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            copyGalleryImageToCache(context, it)?.let { file ->
                onImageCaptured(PhotoEffectProcessor.apply(context, file, selectedEffect).absolutePath)
            }
        }
    }

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

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            if (state.isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    } catch (_: Exception) {
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            update = {
                imageCapture?.flashMode =
                    if (state.flashMode) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MomentaBackground.copy(alpha = 0.68f))
                .navigationBarsPadding()
                .padding(bottom = 16.dp, top = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CameraToolButton(
                    icon = Icons.Default.Image,
                    label = "Галерея",
                    onClick = { galleryLauncher.launch("image/*") }
                )

                CaptureButton(
                    enabled = imageCapture != null,
                    onClick = {
                        capturePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            effect = selectedEffect,
                            onImageCaptured = onImageCaptured
                        )
                    }
                )

                CameraToolButton(
                    icon = Icons.Default.Tune,
                    label = selectedEffect.title,
                    onClick = { showEffects = !showEffects }
                )
            }

            if (showEffects) {
                Spacer(modifier = Modifier.height(12.dp))
                EffectsPanel(
                    selected = selectedEffect,
                    onSelect = {
                        selectedEffect = it
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
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

@Composable
private fun CameraToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(width = 76.dp, height = 62.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = MomentaSurface.copy(alpha = 0.82f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MomentaDivider)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = MomentaText, modifier = Modifier.size(21.dp))
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            color = MomentaTextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CaptureButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MomentaGreen.copy(alpha = 0.18f))
            .border(3.dp, if (enabled) MomentaGreen else MomentaDivider, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(76.dp)) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(MomentaText)
            )
        }
    }
}

@Composable
private fun EffectsPanel(
    selected: PhotoEffect,
    onSelect: (PhotoEffect) -> Unit
) {
    Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
        color = MomentaSurface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                items(PhotoEffect.entries) { effect ->
                    EffectChip(
                        effect = effect,
                        selected = effect == selected,
                        onClick = { onSelect(effect) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = selected.subtitle,
                color = MomentaTextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }
    }
}

@Composable
private fun EffectChip(
    effect: PhotoEffect,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(42.dp)
            .widthIn(min = 98.dp)
            .clickable(onClick = onClick),
        color = if (selected) MomentaGreen.copy(alpha = 0.20f) else MomentaBackground,
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MomentaGreen else MomentaDivider
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = effect.title,
                color = if (selected) MomentaGreen else MomentaText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    effect: PhotoEffect,
    onImageCaptured: (String) -> Unit
) {
    val capture = imageCapture ?: return
    val photoFile = File(
        context.cacheDir,
        "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
    )
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    capture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val finalFile = PhotoEffectProcessor.apply(context, photoFile, effect)
                onImageCaptured(finalFile.absolutePath)
            }

            override fun onError(exception: ImageCaptureException) {
            }
        }
    )
}

private fun copyGalleryImageToCache(context: Context, uri: Uri): File? {
    return try {
        val output = File(
            context.cacheDir,
            "GALLERY_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        )
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(output).use { outputStream -> input.copyTo(outputStream) }
        } ?: return null
        output
    } catch (_: Exception) {
        null
    }
}
