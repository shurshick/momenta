package com.bghitech.momenta.feature.feed

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.rememberAsyncImagePainter
import com.bghitech.momenta.core.design.*
import com.bghitech.momenta.core.util.DateUtils
import com.bghitech.momenta.domain.model.Comment
import com.bghitech.momenta.domain.model.Post

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FeedScreen(
    focusPostId: String? = null,
    onFocusHandled: (String) -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val isRefreshing = state.isLoading && state.items.isNotEmpty()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = viewModel::refresh
    )
    var requestedFocusReloadFor by remember { mutableStateOf<String?>(null) }
    var handledFocusPostId by remember { mutableStateOf<String?>(null) }
    var fullscreenPost by remember { mutableStateOf<Post?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(focusPostId, state.items, state.isLoading) {
        val targetId = focusPostId ?: return@LaunchedEffect
        if (handledFocusPostId == targetId) return@LaunchedEffect
        val index = state.items.indexOfFirst { it.id == targetId }
        if (index >= 0) {
            requestedFocusReloadFor = null
            listState.animateScrollToItem(index)
            handledFocusPostId = targetId
            onFocusHandled(targetId)
        } else if (!state.isLoading && requestedFocusReloadFor != targetId) {
            requestedFocusReloadFor = targetId
            viewModel.loadFeed(force = true)
        }
    }

    state.commentsPost?.let { post ->
        CommentsDialog(
            post = post,
            comments = state.comments,
            isLoading = state.isCommentsLoading,
            error = state.commentsError,
            onDismiss = viewModel::closeComments,
            onSend = viewModel::createComment,
            onDelete = viewModel::deleteComment
        )
    }

    fullscreenPost?.let { selected ->
        val post = state.items.firstOrNull { it.id == selected.id } ?: selected
        MomentaMediaViewer(
            imageUrl = post.previewUrl.ifBlank { post.thumbUrl.orEmpty() },
            title = post.user.displayName ?: post.user.username,
            caption = post.caption,
            isBookmarked = post.isBookmarked,
            onBookmarkClick = { viewModel.toggleBookmark(post.id, post.isBookmarked) },
            onShareClick = { sharePost(context, post) },
            onDismiss = { fullscreenPost = null }
        )
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= listState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !state.isLoadingMore && state.items.isNotEmpty()) {
            viewModel.loadMore()
        }
    }

    MomentaScreen {
        Column(modifier = Modifier.fillMaxSize()) {
        MomentaScreenHeader(title = "Мир сейчас")

        LazyRow(
            modifier = Modifier.height(78.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (state.isLoading && state.suggestedUsers.isEmpty()) {
                items(5) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(50.dp).clip(CircleShape).background(MomentaSurfaceAlt)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier.width(44.dp).height(10.dp).clip(MomentaSmallShape).background(MomentaSurfaceAlt)
                        )
                    }
                }
            } else {
                items(state.suggestedUsers, key = { it.id.ifBlank { it.username } }) { user ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(58.dp)
                    ) {
                        MomentaAvatar(
                            avatarUrl = user.avatarUrl,
                            avatarKey = user.avatarKey,
                            username = user.username,
                            size = 50.dp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = user.displayName ?: user.username,
                            color = MomentaTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (state.isOffline && state.items.isNotEmpty()) {
            MomentaOfflineBanner(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pullRefresh(pullRefreshState)
        ) {
            when {
                state.isLoading && state.items.isEmpty() -> {
                    FeedLoadingSkeleton()
                }
                state.error != null && state.items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error!!,
                                color = MomentaError,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            MomentaPrimaryButton(text = "Повторить", onClick = { viewModel.loadFeed() })
                        }
                    }
                }
                state.items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MomentaCard(
                            modifier = Modifier.padding(24.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                MomentaLogoMark(size = 64)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Пока никто не поделился моментом. Стань первым.",
                                    color = MomentaText,
                                    fontSize = 14.sp,
                                    lineHeight = 19.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.items, key = { it.id }) { post ->
                            FeedPostCard(
                                post = post,
                                onLikeClick = { viewModel.toggleLike(post.id, post.isLiked) },
                                onBookmarkClick = { viewModel.toggleBookmark(post.id, post.isBookmarked) },
                                onCommentsClick = { viewModel.openComments(post) },
                                onOpenContent = { fullscreenPost = post },
                                onReport = { viewModel.reportPost(post.id) },
                                onDelete = { viewModel.deletePost(post.id) }
                            )
                        }

                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MomentaLoadingMark(size = 30)
                                }
                            }
                        }
                    }

                }
            }
            if (isRefreshing || pullRefreshState.progress > 0f) {
                MomentaLoadingMark(
                    size = 38,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    rotating = isRefreshing
                )
            }
        }
        }
    }
}

@Composable
private fun FeedLoadingSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(3) {
            MomentaCard(contentPadding = PaddingValues(0.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MomentaSurfaceAlt)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(12.dp)
                                    .clip(MomentaSmallShape)
                                    .background(MomentaSurfaceAlt)
                            )
                            Box(
                                modifier = Modifier
                                    .width(72.dp)
                                    .height(10.dp)
                                    .clip(MomentaSmallShape)
                                    .background(MomentaSurfaceAlt.copy(alpha = 0.7f))
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.04f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MomentaSurfaceAlt)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.width(170.dp).height(17.dp).clip(MomentaSmallShape).background(MomentaSurfaceAlt)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(4) {
                            Box(
                                modifier = Modifier.width(if (it < 2) 62.dp else 48.dp).height(42.dp)
                                    .clip(RoundedCornerShape(16.dp)).background(MomentaSurfaceAlt)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedPostCard(
    post: Post,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onCommentsClick: () -> Unit,
    onOpenContent: () -> Unit,
    onReport: () -> Unit,
    onDelete: () -> Unit
) {
    var showReportDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Пожаловаться", color = MomentaText) },
            text = { Text("Отправить жалобу на этот момент?", color = MomentaTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showReportDialog = false
                    onReport()
                }) { Text("Отправить", color = MomentaError) }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Отмена", color = MomentaGreen) }
            },
            containerColor = MomentaSurface
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить момент?", color = MomentaText) },
            text = { Text("Удаление доступно только для своих моментов в настроенное окно удаления.", color = MomentaTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Удалить", color = MomentaError) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена", color = MomentaGreen) }
            },
            containerColor = MomentaSurface
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MomentaSurface.copy(alpha = 0.92f),
        shape = MomentaLargeShape,
        border = BorderStroke(1.dp, MomentaGreen.copy(alpha = 0.16f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MomentaAvatar(
                    avatarUrl = post.user.avatarUrl,
                    avatarKey = post.user.avatarKey,
                    username = post.user.username,
                    size = 52.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.user.displayName ?: post.user.username,
                        color = MomentaText,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row {
                        if (post.city != null || post.country != null) {
                            Text(
                                text = listOfNotNull(post.city, post.country).joinToString(", "),
                                color = MomentaTextSecondary, fontSize = 13.sp
                            )
                            Text(" · ", color = MomentaTextSecondary, fontSize = 13.sp)
                        }
                        Text(
                            text = DateUtils.formatRelativeTime(post.createdAt),
                            color = MomentaTextSecondary, fontSize = 13.sp
                        )
                    }
                }

                if (post.canDelete) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить", tint = MomentaTextSecondary, modifier = Modifier.size(22.dp))
                    }
                }

                IconButton(onClick = { showReportDialog = true }) {
                    Icon(Icons.Default.Flag, contentDescription = "Пожаловаться", tint = MomentaTextSecondary, modifier = Modifier.size(24.dp))
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.04f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onOpenContent),
                color = MomentaSurfaceAlt,
                shape = RoundedCornerShape(18.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = post.previewUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            if (!post.caption.isNullOrBlank()) {
                Text(
                    text = post.caption,
                    color = MomentaText,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FeedActionPill(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Лайк",
                        tint = if (post.isLiked) MomentaError else MomentaTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = post.likesCount.toString(), color = MomentaTextSecondary, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                FeedActionPill(onClick = onCommentsClick) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = "Комментарии",
                        tint = MomentaTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = post.commentsCount.toString(), color = MomentaTextSecondary, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                FeedActionPill(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (post.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (post.isBookmarked) "Удалить из избранного" else "В избранное",
                        tint = if (post.isBookmarked) MomentaGreen else MomentaTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                FeedActionPill(onClick = {
                    sharePost(context, post)
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Поделиться", tint = MomentaTextSecondary, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

private fun sharePost(context: android.content.Context, post: Post) {
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
private fun FeedActionPill(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = MomentaBackground.copy(alpha = 0.24f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MomentaTextSecondary.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun CommentsDialog(
    post: Post,
    comments: List<Comment>,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var text by remember(post.id) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Комментарии", color = MomentaText) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when {
                    isLoading -> Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) { MomentaLoadingMark(size = 38) }
                    error != null -> Text(error, color = MomentaError)
                    comments.isEmpty() -> Text("Пока нет комментариев", color = MomentaTextSecondary)
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(comments, key = { it.id }) { comment ->
                            Row(verticalAlignment = Alignment.Top) {
                                MomentaAvatar(
                                    avatarUrl = comment.user.avatarUrl,
                                    avatarKey = comment.user.avatarKey,
                                    username = comment.user.username,
                                    size = 32.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = comment.user.displayName ?: comment.user.username,
                                        color = MomentaText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(comment.text, color = MomentaTextSecondary, fontSize = 13.sp)
                                }
                                if (comment.canDelete) {
                                    IconButton(onClick = { onDelete(comment.id) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить", tint = MomentaTextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it.take(500) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ваш комментарий") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MomentaText,
                            unfocusedTextColor = MomentaText,
                            cursorColor = MomentaGreen,
                            focusedBorderColor = MomentaGreen,
                            unfocusedBorderColor = MomentaDivider,
                            focusedPlaceholderColor = MomentaTextSecondary,
                            unfocusedPlaceholderColor = MomentaTextSecondary
                        )
                    )
                    IconButton(onClick = {
                        onSend(text)
                        text = ""
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить", tint = MomentaGreen)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть", color = MomentaGreen) }
        },
        containerColor = MomentaSurface
    )
}
