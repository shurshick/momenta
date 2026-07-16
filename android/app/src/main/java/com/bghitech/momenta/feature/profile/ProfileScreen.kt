package com.bghitech.momenta.feature.profile

import android.content.Intent
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
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
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
import com.bghitech.momenta.core.design.MomentaLoadingMark
import com.bghitech.momenta.core.design.MomentaMediaViewer
import com.bghitech.momenta.core.design.MomentaMediumShape
import com.bghitech.momenta.core.design.MomentaOfflineBanner
import com.bghitech.momenta.core.design.MomentaPrimaryButton
import com.bghitech.momenta.core.design.MomentaRoundShape
import com.bghitech.momenta.core.design.MomentaScreen
import com.bghitech.momenta.core.design.MomentaSurface
import com.bghitech.momenta.core.design.MomentaSurfaceAlt
import com.bghitech.momenta.core.design.MomentaText
import com.bghitech.momenta.core.design.MomentaTextSecondary
import com.bghitech.momenta.core.design.MomentaWarm
import com.bghitech.momenta.domain.model.Post
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    openEditorOnStart: Boolean = false,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by rememberSaveable { mutableStateOf(openEditorOnStart) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenResumed()
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
                TextButton(onClick = {
                    showLogoutDialog = false
                    scope.launch {
                        viewModel.logout()
                        onLogout()
                    }
                }) {
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
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Профиль",
                    color = MomentaText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MomentaTextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            if (state.isLoading && state.username.isBlank()) {
                ProfileLoadingSkeleton()
            } else if (state.username.isBlank() && state.error != null) {
                ProfileUnavailable(
                    message = state.error ?: "Профиль недоступен",
                    onRetry = { viewModel.loadProfile(force = true) }
                )
            } else {
                if (state.isOffline) {
                    MomentaOfflineBanner(modifier = Modifier.padding(bottom = 8.dp))
                }
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
                    onAvatarClick = { showAvatarDialog = true },
                    onRemoveBookmark = viewModel::removeBookmark,
                    onLoadMoreOwnPosts = viewModel::loadMoreOwnPosts,
                    onLoadMoreBookmarks = viewModel::loadMoreBookmarks,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onEditClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onRemoveBookmark: (Post) -> Unit,
    onLoadMoreOwnPosts: () -> Unit,
    onLoadMoreBookmarks: () -> Unit,
    modifier: Modifier = Modifier
) {
    var previewPost by remember { mutableStateOf<Post?>(null) }
    var showBookmarks by remember { mutableStateOf(false) }
    val context = LocalContext.current

    previewPost?.let { post ->
        MomentaMediaViewer(
            imageUrl = post.previewUrl.ifBlank { post.thumbUrl.orEmpty() },
            title = state.displayName,
            caption = post.caption,
            isBookmarked = post.isBookmarked,
            onBookmarkClick = if (post.isBookmarked) {
                { onRemoveBookmark(post) }
            } else {
                null
            },
            onShareClick = { shareProfilePost(context, post) },
            onDismiss = { previewPost = null }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item(key = "profile-summary") {
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
        }

        item(key = "profile-tabs") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileSectionTab(
                    text = "Мои моменты",
                    selected = !showBookmarks,
                    icon = { Icon(Icons.Default.GridView, null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f),
                    onClick = { showBookmarks = false }
                )
                ProfileSectionTab(
                    text = "Избранное",
                    selected = showBookmarks,
                    icon = { Icon(Icons.Default.Bookmark, null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f),
                    onClick = { showBookmarks = true }
                )
            }
        }

        if (showBookmarks) {
            if (state.bookmarkedPosts.isEmpty()) {
                item(key = "empty-bookmarks") {
                    if (state.isBookmarksLoading) {
                        Text("Загружаем избранное…", color = MomentaTextSecondary, fontSize = 14.sp)
                    } else {
                        EmptyBookmarks()
                    }
                }
            } else {
                lazyItems(
                    items = state.bookmarkedPosts,
                    key = { post -> "bookmark-${post.id}" }
                ) { post ->
                    FavoritePostRow(
                        post = post,
                        onOpen = { previewPost = post },
                        onShare = { shareProfilePost(context, post) },
                        onRemove = { onRemoveBookmark(post) }
                    )
                }
            }
            if (state.bookmarksNextCursor != null) {
                item(key = "more-bookmarks") {
                    LaunchedEffect(state.bookmarksNextCursor) { onLoadMoreBookmarks() }
                    MomentaLoadingMark(size = 30)
                }
            }
        } else {
            val ownPosts = state.ownPosts.ifEmpty { state.recentPosts }
            if (ownPosts.isEmpty() && !state.isOwnPostsLoading) {
                item(key = "empty-own-posts") { EmptyProfileMoments() }
            } else {
                val rows = ownPosts.chunked(3)
                lazyItems(
                    items = rows,
                    key = { row -> "own-row-${row.firstOrNull()?.id.orEmpty()}" }
                ) { rowPosts ->
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
                        repeat(3 - rowPosts.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
            if (state.ownPostsNextCursor != null) {
                item(key = "more-own-posts") {
                    LaunchedEffect(state.ownPostsNextCursor) { onLoadMoreOwnPosts() }
                    MomentaLoadingMark(size = 30)
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionTab(
    text: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(44.dp).clickable(onClick = onClick),
        shape = MomentaMediumShape,
        color = if (selected) MomentaGreen.copy(alpha = 0.16f) else MomentaSurfaceAlt,
        border = BorderStroke(1.dp, if (selected) MomentaGreen.copy(alpha = 0.5f) else MomentaDivider)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides if (selected) MomentaGreen else MomentaTextSecondary
            ) { icon() }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (selected) MomentaGreen else MomentaTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FavoritePostRow(
    post: Post,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MomentaMediumShape,
        color = MomentaSurfaceAlt
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(post.thumbUrl ?: post.previewUrl),
                contentDescription = post.caption ?: "Избранный момент",
                modifier = Modifier.size(82.dp).clip(MomentaMediumShape).clickable(onClick = onOpen),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(
                    text = post.user.displayName ?: post.user.username,
                    color = MomentaText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!post.caption.isNullOrBlank()) {
                    Text(
                        text = post.caption,
                        color = MomentaTextSecondary,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Поделиться", tint = MomentaTextSecondary)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Bookmark, contentDescription = "Удалить из избранного", tint = MomentaGreen)
            }
        }
    }
}

@Composable
private fun EmptyBookmarks() {
    MomentaCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Сохранённые моменты появятся здесь",
            color = MomentaTextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

private fun shareProfilePost(context: android.content.Context, post: Post) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "Момент от ${post.user.displayName ?: post.user.username} в Момента: ${post.previewUrl}"
        )
    }
    context.startActivity(Intent.createChooser(intent, "Поделиться моментом"))
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
        Box(
            modifier = Modifier
                .size(114.dp)
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
        }

        Spacer(modifier = Modifier.height(4.dp))

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
            accent = MomentaGreen
        )
        ProfileStatTile(
            value = momentsCount,
            label = momentWord(momentsCount).lowercase(),
            accent = MomentaGreen
        )
        ProfileStatTile(
            value = likesCount,
            label = likeWord(likesCount).lowercase(),
            accent = MomentaWarm
        )
    }
}

@Composable
private fun ProfileStatTile(
    value: Int,
    label: String,
    accent: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = MomentaSurface.copy(alpha = 0.9f),
        shape = MomentaLargeShape,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$value",
                color = accent,
                fontSize = 27.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = MomentaTextSecondary,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center
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
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(114.dp)
                        .clip(CircleShape)
                        .background(MomentaSurfaceAlt)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SkeletonBlock(width = 112.dp, height = 30.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MomentaSurfaceAlt)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonBlock(width = 92.dp, height = 18.dp)
                Spacer(modifier = Modifier.height(10.dp))
                SkeletonBlock(width = 142.dp, height = 17.dp)
                Spacer(modifier = Modifier.height(4.dp))
                SkeletonBlock(width = 118.dp, height = 17.dp)
            }
            Column(
                modifier = Modifier.width(152.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { SkeletonStat() }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(MomentaMediumShape)
                        .background(MomentaSurfaceAlt)
                )
            }
        }
        Spacer(modifier = Modifier.height(7.dp))
        repeat(3) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun SkeletonStat() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(72.dp),
        color = MomentaSurface.copy(alpha = 0.9f),
        shape = MomentaLargeShape,
        border = BorderStroke(1.dp, MomentaGreen.copy(alpha = 0.12f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SkeletonBlock(width = 42.dp, height = 27.dp)
            Spacer(modifier = Modifier.height(3.dp))
            SkeletonBlock(width = 64.dp, height = 13.dp)
        }
    }
}

@Composable
private fun ProfileUnavailable(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MomentaLogoMark(size = 64)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = message,
            color = MomentaTextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        MomentaPrimaryButton(text = "Повторить", onClick = onRetry)
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
