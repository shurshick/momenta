package com.bghitech.momenta.feature.feed

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.bghitech.momenta.core.design.*
import com.bghitech.momenta.core.util.DateUtils
import com.bghitech.momenta.domain.model.Post

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var selectedTab by remember { mutableIntStateOf(0) }

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

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Мир сейчас",
            color = MomentaText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        // Tabs: Мир сейчас / Подписки
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

        // Horizontal avatars row
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

            items(5) { index ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MomentaSurfaceAlt),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("•", color = MomentaGreen, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = listOf("Аня", "Макс", "Лера", "Кирилл", "Даша")[index],
                        color = MomentaTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        if (state.isLoading && state.items.isEmpty()) {
            MomentaLoading()
            return
        }

        if (state.error != null && state.items.isEmpty()) {
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
            return
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.items, key = { it.id }) { post ->
                FeedPostCard(post = post, onLikeClick = { viewModel.toggleLike(post.id, post.isLiked) })
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
    }
}

@Composable
private fun FeedPostCard(
    post: Post,
    onLikeClick: () -> Unit
) {
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Пожаловаться", color = MomentaText) },
            text = { Text("Отправить жалобу на этот момент?", color = MomentaTextSecondary) },
            confirmButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Отправить", color = MomentaError) }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Отмена", color = MomentaGreen) }
            },
            containerColor = MomentaSurface
        )
    }

    MomentaCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
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

                IconButton(onClick = { showReportDialog = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Ещё", tint = MomentaTextSecondary, modifier = Modifier.size(20.dp))
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

                IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = "Комментарии",
                        tint = MomentaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

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
