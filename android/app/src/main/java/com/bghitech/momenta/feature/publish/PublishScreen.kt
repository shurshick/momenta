package com.bghitech.momenta.feature.publish

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.bghitech.momenta.core.design.*

@Composable
fun PublishScreen(
    imagePath: String,
    onBack: () -> Unit,
    onUploadSuccess: () -> Unit,
    viewModel: PublishViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var caption by remember { mutableStateOf("") }

    LaunchedEffect(imagePath) {
        viewModel.loadImage(imagePath)
    }

    LaunchedEffect(state.uploaded) {
        if (state.uploaded) onUploadSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Назад", tint = MomentaText)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Новый момент",
                color = MomentaText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(48.dp))
        }

        if (state.isCompressing) {
            MomentaLoading(message = "Обработка изображения…")
        } else {
            Image(
                painter = rememberAsyncImagePainter(model = imagePath),
                contentDescription = "Preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Подпись") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MomentaText,
                    unfocusedTextColor = MomentaText,
                    cursorColor = MomentaGreen,
                    focusedBorderColor = MomentaGreen,
                    unfocusedBorderColor = MomentaDivider,
                    focusedLabelColor = MomentaGreen,
                    unfocusedLabelColor = MomentaTextSecondary
                ),
                minLines = 3,
                maxLines = 5
            )

            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.error!!,
                    color = MomentaError,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            MomentaPrimaryButton(
                text = if (state.isUploading) "Публикация…" else "Опубликовать",
                onClick = {
                    viewModel.publish("today", caption.ifBlank { null }, null, null)
                },
                loading = state.isUploading,
                enabled = !state.isCompressing && !state.isUploading,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
