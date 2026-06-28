package com.bghitech.momenta.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.bghitech.momenta.core.design.MomentaCard
import com.bghitech.momenta.core.design.MomentaAvatar
import com.bghitech.momenta.core.design.MomentaDivider
import com.bghitech.momenta.core.design.MomentaError
import com.bghitech.momenta.core.design.MomentaGreen
import com.bghitech.momenta.core.design.MomentaGreenAlpha
import com.bghitech.momenta.core.design.MomentaLoading
import com.bghitech.momenta.core.design.MomentaMediumShape
import com.bghitech.momenta.core.design.MomentaRoundShape
import com.bghitech.momenta.core.design.MomentaScreen
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
    var showAvatarDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadProfile(force = true, showLoading = false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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

    if (showAvatarDialog) {
        AvatarPickerDialog(
            state = state,
            onDismiss = { showAvatarDialog = false },
            onSelect = { avatarKey ->
                viewModel.updateAvatar(avatarKey)
                showAvatarDialog = false
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
                    onEditClick = { showEditDialog = true },
                    onAvatarClick = { showAvatarDialog = true }
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onEditClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            MomentaAvatar(
                avatarUrl = state.avatarUrl,
                avatarKey = state.avatarKey,
                username = state.username,
                size = 78.dp,
                modifier = Modifier.clickable(onClick = onAvatarClick)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.displayName,
                    color = MomentaText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать профиль",
                        tint = MomentaGreen,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

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
                    lineHeight = 17.sp
                )
            }
        }

        ProfileStatsBlock(
            state = state,
            modifier = Modifier.weight(1.25f)
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

    Text(
        text = "Недавние моменты",
        color = MomentaTextSecondary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(8.dp))

    if (state.recentPosts.isEmpty()) {
        MomentaCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Здесь появятся твои опубликованные моменты.",
                color = MomentaTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            state.recentPosts.take(9).chunked(3).forEach { rowPosts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowPosts.forEach { post ->
                        RecentPostTile(
                            post = post,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - rowPosts.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatsBlock(state: ProfileUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatItem(
            value = "${state.streakCount}",
            label = "${dayWord(state.streakCount)}\nподряд",
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatItem(
                value = "${state.momentsCount}",
                label = momentWord(state.momentsCount),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
            )
            StatItem(
                value = "${state.likesCount}",
                label = likeWord(state.likesCount),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
            )
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
private fun AvatarPickerDialog(
    state: ProfileUiState,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выбрать аватар", color = MomentaText) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.avatarOptions, key = { it }) { avatarKey ->
                    val selected = avatarKey == state.avatarKey
                    Surface(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(MomentaRoundShape)
                            .clickable { onSelect(avatarKey) },
                        color = if (selected) MomentaGreenAlpha else MomentaSurfaceAlt
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            MomentaAvatar(
                                avatarUrl = null,
                                avatarKey = avatarKey,
                                username = state.username.removePrefix("@"),
                                size = 58.dp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = MomentaGreen)
            }
        },
        containerColor = MomentaSurface
    )
}

@Composable
private fun RecentPostTile(post: Post, modifier: Modifier = Modifier) {
    val imageUrl = post.thumbUrl ?: post.previewUrl
    Surface(
        modifier = modifier
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
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
) {
    MomentaCard(modifier = modifier, contentPadding = contentPadding) {
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
                lineHeight = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun dayWord(value: Int): String = pluralRu(value, "день", "дня", "дней")

private fun momentWord(value: Int): String = pluralRu(value, "Момент", "Момента", "Моментов")

private fun likeWord(value: Int): String = pluralRu(value, "Лайк", "Лайка", "Лайков")

private fun pluralRu(value: Int, one: String, few: String, many: String): String {
    val mod100 = value % 100
    val mod10 = value % 10
    return when {
        mod100 in 11..14 -> many
        mod10 == 1 -> one
        mod10 in 2..4 -> few
        else -> many
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
