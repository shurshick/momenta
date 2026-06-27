package com.bghitech.momenta.feature.feed

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.bghitech.momenta.core.design.*
import com.bghitech.momenta.core.util.DateUtils
import com.bghitech.momenta.domain.model.Comment
import com.bghitech.momenta.domain.model.Post
import kotlin.math.roundToInt

@Composable
fun FeedScreen(
    forceRefreshOnOpen: Boolean = false,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var pullOffset by remember { mutableFloatStateOf(0f) }
    val isRefreshing = state.isLoading && state.items.isNotEmpty()
    var handledForcedRefresh by remember { mutableStateOf(false) }

    LaunchedEffect(forceRefreshOnOpen, state.isLoading) {
        if (forceRefreshOnOpen && !handledForcedRefresh && !state.isLoading) {
            handledForcedRefresh = true
            viewModel.refreshAfterPublish()
        }
    }

    LaunchedEffect(state.scrollToTopSignal) {
        if (state.scrollToTopSignal > 0) {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing && pullOffset > 0f) {
            pullOffset = 0f
        }
    }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isRefreshing) 80f else pullOffset,
        label = "pullOffset"
    )

    val density = LocalDensity.current

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
        Text(
            text = "Мир сейчас",
            color = MomentaText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            listOf("Мир сейчас", "Подписки").forEachIndexed { index, title ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = index }
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = if (selectedTab == index) MomentaGreen else MomentaTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                    )
                    if (selectedTab == index) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(MomentaGreen, MomentaSmallShape)
                        )
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { }
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MomentaGreen)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MomentaSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Ваш момент",
                                tint = MomentaGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Ваш момент", color = MomentaTextSecondary, fontSize = 10.sp)
                }
            }

            items(state.suggestedUsers, key = { it.id.ifBlank { it.username } }) { user ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(64.dp)
                ) {
                    MomentaAvatar(
                        avatarUrl = user.avatarUrl,
                        avatarKey = user.avatarKey,
                        username = user.username,
                        size = 52.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.displayName ?: user.username,
                        color = MomentaTextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                state.isLoading && state.items.isEmpty() -> {
                    MomentaLoading()
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
                        MomentaCard(modifier = Modifier.padding(24.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                MomentaLogoMark(size = 64)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Пока никто не поделился моментом. Стань первым.",
                                    color = MomentaText,
                                    fontSize = 15.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset(0, animatedOffset.roundToInt()) }
                            .pointerInput(isRefreshing) {
                                if (!isRefreshing) {
                                    detectVerticalDragGestures(
                                        onDragEnd = {
                                            if (pullOffset > 120f) {
                                                viewModel.refresh()
                                            } else {
                                                pullOffset = 0f
                                            }
                                        },
                                        onVerticalDrag = { change, dragAmount ->
                                            val scrollUp = listState.firstVisibleItemIndex == 0 &&
                                                    listState.firstVisibleItemScrollOffset == 0
                                            if (scrollUp && dragAmount > 0f) {
                                                change.consume()
                                                pullOffset = (pullOffset + dragAmount / density.density).coerceIn(0f, 200f)
                                            } else if (dragAmount < 0f || pullOffset > 0f) {
                                                change.consume()
                                                pullOffset = (pullOffset + dragAmount / density.density).coerceAtLeast(0f)
                                            }
                                        }
                                    )
                                }
                            },
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.items, key = { it.id }) { post ->
                            FeedPostCard(
                                post = post,
                                onLikeClick = { viewModel.toggleLike(post.id, post.isLiked) },
                                onCommentsClick = { viewModel.openComments(post) },
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
                                    CircularProgressIndicator(color = MomentaGreen, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            }
                        }
                    }

                    if (animatedOffset > 10f) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 12.dp)
                                .size(24.dp),
                            color = MomentaGreen,
                            strokeWidth = 2.dp,
                            progress = { (animatedOffset / 120f).coerceIn(0f, 1f) }
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
    onCommentsClick: () -> Unit,
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
            text = { Text("Удаление доступно только для своих моментов в первые 24 часа.", color = MomentaTextSecondary) },
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

    MomentaCard(contentPadding = PaddingValues(0.dp)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MomentaAvatar(
                    avatarUrl = post.user.avatarUrl,
                    avatarKey = post.user.avatarKey,
                    username = post.user.username,
                    size = 36.dp
                )
                Box(modifier = Modifier.size(0.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MomentaSurfaceAlt, shape = CircleShape) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("•", color = MomentaGreen, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = post.user.displayName ?: post.user.username,
                        color = MomentaText, fontSize = 14.sp, fontWeight = FontWeight.Medium
                    )
                    Row {
                        if (post.city != null || post.country != null) {
                            Text(
                                text = listOfNotNull(post.city, post.country).joinToString(", "),
                                color = MomentaTextSecondary, fontSize = 11.sp
                            )
                            Text(" · ", color = MomentaTextSecondary, fontSize = 11.sp)
                        }
                        Text(
                            text = DateUtils.formatRelativeTime(post.createdAt),
                            color = MomentaTextSecondary, fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (post.canDelete) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить", tint = MomentaTextSecondary, modifier = Modifier.size(20.dp))
                    }
                }

                IconButton(onClick = { showReportDialog = true }) {
                    Icon(Icons.Default.Flag, contentDescription = "Пожаловаться", tint = MomentaTextSecondary, modifier = Modifier.size(20.dp))
                }
            }

            Surface(modifier = Modifier.fillMaxWidth().height(300.dp), color = MomentaSurfaceAlt) {
                Image(
                    painter = rememberAsyncImagePainter(model = post.previewUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            if (post.caption != null) {
                Text(
                    text = post.caption, color = MomentaText, fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLikeClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Лайк",
                        tint = if (post.isLiked) MomentaError else MomentaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(text = post.likesCount.toString(), color = MomentaTextSecondary, fontSize = 12.sp)

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onCommentsClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = "Комментарии",
                        tint = MomentaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(text = post.commentsCount.toString(), color = MomentaTextSecondary, fontSize = 12.sp)

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Момент от ${post.user.displayName ?: post.user.username} в Момента: ${post.previewUrl}")
                    }
                    context.startActivity(Intent.createChooser(intent, "Поделиться моментом"))
                }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Поделиться", tint = MomentaTextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }
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
                    ) { CircularProgressIndicator(color = MomentaGreen) }
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
                        maxLines = 3
                    )
                    IconButton(onClick = {
                        onSend(text)
                        text = ""
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "Отправить", tint = MomentaGreen)
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
