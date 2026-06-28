package com.bghitech.momenta.feature.publish

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.bghitech.momenta.R
import com.bghitech.momenta.core.design.MomentaBackground
import com.bghitech.momenta.core.design.MomentaCard
import com.bghitech.momenta.core.design.MomentaDivider
import com.bghitech.momenta.core.design.MomentaError
import com.bghitech.momenta.core.design.MomentaGreen
import com.bghitech.momenta.core.design.MomentaLoading
import com.bghitech.momenta.core.design.MomentaScreen
import com.bghitech.momenta.core.design.MomentaSurface
import com.bghitech.momenta.core.design.MomentaText
import com.bghitech.momenta.core.design.MomentaTextSecondary

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

    MomentaScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = MomentaText)
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
                MomentaLoading(message = "Обрабатываем изображение...")
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imagePath),
                        contentDescription = "Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )

                    if (state.error != null) {
                        Text(
                            text = stringResource(R.string.upload_retry_later),
                            color = MomentaError,
                            fontSize = 13.sp
                        )
                    }
                }

                MomentaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    containerColor = MomentaSurface.copy(alpha = 0.96f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = caption,
                            onValueChange = { caption = it },
                            label = { Text(stringResource(R.string.caption)) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MomentaText,
                                unfocusedTextColor = MomentaText,
                                cursorColor = MomentaGreen,
                                focusedBorderColor = MomentaGreen,
                                unfocusedBorderColor = MomentaDivider,
                                focusedLabelColor = MomentaGreen,
                                unfocusedLabelColor = MomentaTextSecondary
                            ),
                            minLines = 1,
                            maxLines = 3
                        )

                        Surface(
                            modifier = Modifier.size(50.dp),
                            shape = CircleShape,
                            color = if (state.isUploading) MomentaDivider else MomentaGreen,
                            shadowElevation = 0.dp
                        ) {
                            IconButton(
                                enabled = !state.isUploading,
                                onClick = {
                                    viewModel.publish("today", caption.ifBlank { null }, null, null)
                                }
                            ) {
                                if (state.isUploading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = MomentaText,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = stringResource(R.string.publish),
                                        tint = MomentaBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
