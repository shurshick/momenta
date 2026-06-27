package com.bghitech.momenta.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.bghitech.momenta.core.design.MomentaCard
import com.bghitech.momenta.core.design.MomentaDivider
import com.bghitech.momenta.core.design.MomentaError
import com.bghitech.momenta.core.design.MomentaGreen
import com.bghitech.momenta.core.design.MomentaGreenAlpha
import com.bghitech.momenta.core.design.MomentaLoading
import com.bghitech.momenta.core.design.MomentaMediumShape
import com.bghitech.momenta.core.design.MomentaRoundShape
import com.bghitech.momenta.core.design.MomentaScreen
import com.bghitech.momenta.core.design.MomentaSecondaryButton
import com.bghitech.momenta.core.design.MomentaSurface
import com.bghitech.momenta.core.design.MomentaSurfaceAlt
import com.bghitech.momenta.core.design.MomentaText
import com.bghitech.momenta.core.design.MomentaTextSecondary
import com.bghitech.momenta.domain.model.Post

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditProfileDialog(
            state = state,
            onDismiss = { showEditDialog = false },
            onSave = { displayName, bio ->
                viewModel.updateProfile(displayName, bio)
                showEditDialog = false
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выйти", color = MomentaText) },
            text = { Text("Вы уверены, что хотите выйти?", color = MomentaTextSecondary) },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Выйти", color = MomentaError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена", color = MomentaGreen)
                }
            },
            containerColor = MomentaSurface
        )
    }

    MomentaScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, "Настройки", tint = MomentaTextSecondary)
                }
            }

            if (state.isLoading) {
                MomentaLoading()
            } else {
                ProfileContent(
                    state = state,
                    onEditClick = { showEditDialog = true }
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(MomentaRoundShape),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MomentaSurfaceAlt,
            shape = MomentaRoundShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (state.avatarUrl.isNullOrBlank()) {
                    Text("•", color = MomentaGreen, fontSize = 32.sp)
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(model = state.avatarUrl),
                        contentDescription = state.displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = state.displayName,
        color = MomentaText,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    Text(
        text = state.username,
        color = MomentaTextSecondary,
        fontSize = 14.sp
    )

    if (!state.bio.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.bio,
            color = MomentaTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatItem(value = "${state.momentsCount}", label = "Моментов", modifier = Modifier.weight(1f))
        StatItem(value = "${state.streakCount}", label = "Дней подряд", modifier = Modifier.weight(1f))
        StatItem(value = "${state.likesCount}", label = "Лайков", modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(24.dp))

    MomentaSecondaryButton(
        text = "Редактировать",
        onClick = onEditClick
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Недавние моменты",
        color = MomentaTextSecondary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(8.dp))

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (state.recentPosts.isEmpty()) {
            item(span = { GridItemSpan(3) }) {
                MomentaCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Здесь появятся твои опубликованные моменты.",
                        color = MomentaTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            items(state.recentPosts, key = { it.id }) { post ->
                RecentPostTile(post = post)
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    state: ProfileUiState,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var displayName by remember(state.displayName) { mutableStateOf(state.displayName) }
    var bio by remember(state.bio) { mutableStateOf(state.bio.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать профиль", color = MomentaText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Имя") },
                    singleLine = true,
                    colors = profileFieldColors()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("О себе") },
                    minLines = 2,
                    maxLines = 4,
                    colors = profileFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(displayName, bio) }, enabled = !state.isSaving) {
                Text("Сохранить", color = MomentaGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = MomentaTextSecondary)
            }
        },
        containerColor = MomentaSurface
    )
}

@Composable
private fun RecentPostTile(post: Post) {
    val imageUrl = post.thumbUrl ?: post.previewUrl
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(MomentaMediumShape),
        color = MomentaSurfaceAlt
    ) {
        if (imageUrl.isNotBlank()) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = post.caption ?: "Момент",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text("•", color = MomentaGreenAlpha, fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    MomentaCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                color = MomentaText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = MomentaTextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun profileFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MomentaText,
    unfocusedTextColor = MomentaText,
    cursorColor = MomentaGreen,
    focusedBorderColor = MomentaGreen,
    unfocusedBorderColor = MomentaDivider,
    focusedLabelColor = MomentaGreen,
    unfocusedLabelColor = MomentaTextSecondary
)
