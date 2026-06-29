package com.bghitech.momenta.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import com.bghitech.momenta.core.design.MomentaLargeShape
import com.bghitech.momenta.core.design.MomentaLogoMark
import com.bghitech.momenta.core.design.MomentaMediumShape
import com.bghitech.momenta.core.design.MomentaRoundShape
import com.bghitech.momenta.core.design.MomentaScreen
import com.bghitech.momenta.core.design.MomentaSurface
import com.bghitech.momenta.core.design.MomentaSurfaceAlt
import com.bghitech.momenta.core.design.MomentaText
import com.bghitech.momenta.core.design.MomentaTextSecondary
import com.bghitech.momenta.core.design.MomentaWarm
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

    LaunchedEffect(Unit) {
        viewModel.loadProfile(force = true, showLoading = false)
    }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, "Настройки", tint = MomentaTextSecondary)
                }
            }

            if (state.isLoading) {
                ProfileLoadingSkeleton()
            } else {
                val error = state.error
                if (error != null) {
                    Text(
                        text = error,
                        color = MomentaError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
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
    var previewPost by remember { mutableStateOf<Post?>(null) }

    previewPost?.let { post ->
        RecentPostPreviewDialog(
            post = post,
            onDismiss = { previewPost = null }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        ProfileIdentityBlock(
            state = state,
            onEditClick = onEditClick,
            onAvatarClick = onAvatarClick,
            modifier = Modifier.weight(1f)
        )

        ProfileStatsColumn(
            streakCount = state.streakCount,
            momentsCount = state.momentsCount,
            likesCount = state.likesCount,
            modifier = Modifier.width(152.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MomentaLogoMark(size = 28)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Недавние моменты",
                color = MomentaText,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (state.recentPosts.size > 9) {
            Text(
                text = "Смотреть все ›",
                color = MomentaGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    Spacer(modifier = Modifier.height(7.dp))

    if (state.recentPosts.isEmpty()) {
        EmptyProfileMoments()
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
                            modifier = Modifier.weight(1f),
                            onClick = { previewPost = post }
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
private fun ProfileIdentityBlock(
    state: ProfileUiState,
    onEditClick: () -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            modifier = Modifier
                .size(114.dp)
                .clip(CircleShape)
                .clickable(onClick = onAvatarClick),
            color = Color.Transparent,
            shape = CircleShape,
            border = BorderStroke(2.dp, MomentaGreen.copy(alpha = 0.82f))
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MomentaGreen.copy(alpha = 0.22f),
                                MomentaWarm.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                MomentaAvatar(
                    avatarUrl = state.avatarUrl,
                    avatarKey = state.avatarKey,
                    username = state.username,
                    size = 100.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = state.displayName,
                color = MomentaText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Редактировать профиль",
                    tint = MomentaGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Text(
            text = state.username,
            color = MomentaTextSecondary,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (!state.bio.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = state.bio,
                color = MomentaTextSecondary,
                fontSize = 17.sp,
                lineHeight = 21.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileStatsColumn(
    streakCount: Int,
    momentsCount: Int,
    likesCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProfileStatTile(
            value = streakCount,
            label = "${dayWord(streakCount)}\nподряд",
            accent = MomentaGreen,
            icon = { ProfileStreakGlyph() }
        )
        ProfileStatTile(
            value = momentsCount,
            label = momentWord(momentsCount).lowercase(),
            accent = MomentaGreen,
            icon = { ProfileMomentGlyph() }
        )
        ProfileStatTile(
            value = likesCount,
            label = likeWord(likesCount).lowercase(),
            accent = MomentaWarm,
            icon = {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = MomentaWarm,
                    modifier = Modifier.size(28.dp)
                )
            }
        )
    }
}

@Composable
private fun ProfileStatTile(
    value: Int,
    label: String,
    accent: Color,
    icon: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        color = MomentaSurface.copy(alpha = 0.9f),
        shape = MomentaLargeShape,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$value",
                    color = MomentaText,
                    fontSize = 25.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = label,
                    color = MomentaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun ProfileStreakGlyph() {
    Box(
        modifier = Modifier.size(34.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .border(3.dp, MomentaGreen, CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(12.dp)
                .clip(CircleShape)
                .background(MomentaGreen)
        )
    }
}

@Composable
private fun ProfileMomentGlyph() {
    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "M",
            color = MomentaGreen,
            fontSize = 29.sp,
            lineHeight = 29.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(6.dp)
                .clip(CircleShape)
                .background(MomentaWarm)
        )
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
            Column {
                Text(
                    text = "Нажми на портрет, чтобы поставить его в профиль.",
                    color = MomentaTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
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
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) MomentaGreen else MomentaDivider,
                                    shape = MomentaRoundShape
                                )
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
private fun ProfileLoadingSkeleton() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                        .background(MomentaSurfaceAlt)
                )
                Spacer(modifier = Modifier.height(14.dp))
                SkeletonBlock(width = 130.dp, height = 22.dp)
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonBlock(width = 96.dp, height = 14.dp)
                Spacer(modifier = Modifier.height(12.dp))
                SkeletonBlock(width = 150.dp, height = 14.dp)
            }
            Column(
                modifier = Modifier.weight(1.25f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonStat(fill = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonStat(modifier = Modifier.weight(1f))
                    SkeletonStat(modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        SkeletonBlock(width = 168.dp, height = 20.dp)
        Spacer(modifier = Modifier.height(8.dp))
        repeat(3) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(MomentaMediumShape)
                            .background(MomentaSurfaceAlt)
                    )
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
private fun SkeletonStat(modifier: Modifier = Modifier, fill: Boolean = false) {
    MomentaCard(
        modifier = if (fill) Modifier.fillMaxWidth() else modifier,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SkeletonBlock(width = 36.dp, height = 22.dp)
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonBlock(width = 58.dp, height = 12.dp)
        }
    }
}

@Composable
private fun SkeletonBlock(width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(MomentaLargeShape)
            .background(MomentaSurfaceAlt)
    )
}

@Composable
private fun RecentPostTile(
    post: Post,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val imageUrl = post.thumbUrl ?: post.previewUrl
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(MomentaMediumShape)
            .clickable(onClick = onClick),
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
private fun EmptyProfileMoments() {
    MomentaCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 22.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MomentaLogoMark(size = 54)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Пока нет моментов",
                color = MomentaText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Первый снимок появится здесь красивой галереей.",
                color = MomentaTextSecondary,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecentPostPreviewDialog(post: Post, onDismiss: () -> Unit) {
    val imageUrl = post.previewUrl.ifBlank { post.thumbUrl.orEmpty() }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MomentaLargeShape),
            color = MomentaSurface
        ) {
            Column {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = post.caption ?: "Момент",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
                if (!post.caption.isNullOrBlank()) {
                    Text(
                        text = post.caption,
                        color = MomentaText,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
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
                fontSize = 19.sp,
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
