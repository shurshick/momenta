package com.bghitech.momenta.feature.today

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.bghitech.momenta.R
import com.bghitech.momenta.core.design.MomentaAvatar
import com.bghitech.momenta.core.design.MomentaCard
import com.bghitech.momenta.core.design.MomentaGreen
import com.bghitech.momenta.core.design.MomentaLargeShape
import com.bghitech.momenta.core.design.MomentaLogoMark
import com.bghitech.momenta.core.design.MomentaPrimaryButton
import com.bghitech.momenta.core.design.MomentaScreen
import com.bghitech.momenta.core.design.MomentaSecondaryButton
import com.bghitech.momenta.core.design.MomentaSurfaceAlt
import com.bghitech.momenta.core.design.MomentaText
import com.bghitech.momenta.core.design.MomentaTextSecondary
import com.bghitech.momenta.core.design.MomentaWarm
import com.bghitech.momenta.core.design.rememberCountdownTime
import com.bghitech.momenta.domain.model.Challenge
import com.bghitech.momenta.domain.model.Post

@Composable
fun TodayScreen(
    onCaptureClick: () -> Unit,
    onOpenFeed: () -> Unit,
    onOpenBestPost: (String) -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshBestMoment()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    MomentaScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Header()

            ChallengeSection(
                state = state,
                onRetry = viewModel::loadChallenge,
                onCaptureClick = onCaptureClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            BestMomentSection(
                state = state,
                onCaptureClick = onCaptureClick,
                onOpenBestPost = onOpenBestPost
            )

            Spacer(modifier = Modifier.height(14.dp))

            FeedCallToAction(onClick = onOpenFeed)

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            MomentaLogoMark(size = 34)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.today_title),
                color = MomentaGreen,
                fontSize = 31.sp,
                lineHeight = 33.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = stringResource(R.string.slogan),
            color = MomentaWarm,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChallengeSection(
    state: TodayUiState,
    onRetry: () -> Unit,
    onCaptureClick: () -> Unit
) {
    when {
        state.challenge != null -> ChallengeCard(
            challenge = state.challenge,
            userPostedToday = state.userPostedToday || state.challenge.userPosted,
            onCaptureClick = onCaptureClick
        )
        state.isChallengeLoading -> ChallengeSkeleton()
        else -> SmallErrorCard(
            title = "Задание дня",
            message = state.challengeError ?: "Не удалось загрузить задание дня",
            action = "Повторить",
            onAction = onRetry
        )
    }
}

@Composable
private fun ChallengeCard(
    challenge: Challenge,
    userPostedToday: Boolean,
    onCaptureClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MomentaLargeShape),
        shape = MomentaLargeShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MomentaGreen.copy(alpha = 0.55f))
    ) {
        Box(
            modifier = Modifier.background(
                Brush.radialGradient(
                    colors = listOf(
                        MomentaGreen.copy(alpha = 0.16f),
                        MomentaSurfaceAlt.copy(alpha = 0.96f),
                        MomentaSurfaceAlt
                    ),
                    radius = 760f
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(MomentaGreen)
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChallengeBadge()
                    ParticipantsBadge(count = challenge.participantsCount)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1.1f)) {
                        Text(
                            text = challenge.title.ifBlank { stringResource(R.string.default_challenge_title) },
                            color = MomentaText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 27.sp
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = challenge.prompt ?: challenge.description ?: stringResource(R.string.default_challenge_description),
                            color = MomentaTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 17.sp
                        )
                    }

                    ChallengeIllustration(
                        modifier = Modifier.weight(0.92f)
                    )
                }

                if (challenge.endsAt != null) {
                    val timeLeft = rememberCountdownTime(challenge.endsAt)
                    if (timeLeft.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            TimeLeftPanel(
                                timeLeft = timeLeft,
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(82.dp)
                            )
                            if (userPostedToday) {
                                PostedStatePanel(
                                    modifier = Modifier
                                        .weight(0.92f)
                                        .height(82.dp)
                                )
                            } else {
                                CaptureIconButton(
                                    modifier = Modifier
                                        .weight(0.92f)
                                        .height(82.dp),
                                    onClick = onCaptureClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeBadge() {
    Row(
        modifier = Modifier
            .clip(MomentaLargeShape)
            .background(MomentaGreen.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(2.dp, MomentaGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(MomentaGreen)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Задание дня",
            color = MomentaGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ParticipantsBadge(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Groups,
            contentDescription = null,
            tint = MomentaTextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$count участвуют",
            color = MomentaTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TimeLeftPanel(timeLeft: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MomentaLargeShape)
            .background(MomentaSurfaceAlt.copy(alpha = 0.72f))
            .border(1.dp, MomentaTextSecondary.copy(alpha = 0.12f), MomentaLargeShape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.AccessTime,
                contentDescription = null,
                tint = MomentaWarm,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "До конца дня",
                color = MomentaWarm,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = timeLeft,
            color = MomentaWarm,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 23.sp
        )
    }
}

@Composable
private fun PostedStatePanel(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MomentaLargeShape)
            .background(MomentaSurfaceAlt.copy(alpha = 0.62f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = MomentaGreen.copy(alpha = 0.14f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(62.dp)
        )
        Text(
            text = "Ты уже поделился\nмоментом сегодня",
            color = MomentaText,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CaptureIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MomentaLargeShape)
            .clickable(onClick = onClick),
        color = MomentaGreen,
        shape = MomentaLargeShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = stringResource(R.string.capture_moment),
                tint = Color(0xFF07100B),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun ChallengeIllustration(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.challenge_camera_art),
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .height(108.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun BestMomentSection(
    state: TodayUiState,
    onCaptureClick: () -> Unit,
    onOpenBestPost: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MomentaWarm.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MomentaWarm,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Лучший момент дня",
                color = MomentaText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        when {
            state.bestPost != null -> {
                val post = state.bestPost
                BestMomentCard(post = post, onClick = { onOpenBestPost(post.id) })
            }
            state.isBestMomentLoading -> BestMomentSkeleton()
            else -> EmptyBestMoment(onCaptureClick = onCaptureClick)
        }
    }
}

@Composable
private fun ChallengeSkeleton() {
    MomentaCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonLine(width = 88.dp, height = 12.dp)
                SkeletonLine(width = 96.dp, height = 12.dp)
            }
            SkeletonLine(width = 160.dp, height = 22.dp)
            SkeletonLine(width = 240.dp, height = 14.dp)
            SkeletonLine(width = 132.dp, height = 14.dp, warm = true)
            SkeletonLine(width = 180.dp, height = 40.dp)
        }
    }
}

@Composable
private fun BestMomentSkeleton() {
    MomentaCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.18f)
                    .background(MomentaSurfaceAlt)
            )
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonLine(width = 120.dp, height = 16.dp)
                SkeletonLine(width = 210.dp, height = 13.dp)
            }
        }
    }
}

@Composable
private fun SkeletonLine(width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp, warm: Boolean = false) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(MomentaLargeShape)
            .background(
                if (warm) MomentaWarm.copy(alpha = 0.24f)
                else MomentaSurfaceAlt
            )
    )
}

@Composable
private fun BestMomentCard(post: Post, onClick: () -> Unit) {
    MomentaCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = post.thumbUrl ?: post.previewUrl),
                contentDescription = post.caption ?: "Лучший момент дня",
                modifier = Modifier
                    .weight(1.25f)
                    .aspectRatio(1.05f)
                    .clip(MomentaLargeShape),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MomentaAvatar(
                        avatarUrl = post.user.avatarUrl,
                        avatarKey = post.user.avatarKey,
                        username = post.user.username,
                        size = 38.dp
                    )
                    Spacer(modifier = Modifier.width(9.dp))
                    Column {
                        Text(
                            text = post.user.displayName ?: post.user.username,
                            color = MomentaText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = "сегодня",
                            color = MomentaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = post.caption?.takeIf { it.isNotBlank() } ?: "Момент, который сегодня поймал больше всего тепла.",
                    color = MomentaTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MomentaWarm,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = post.likesCount.toString(),
                        color = MomentaText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MomentaSurfaceAlt),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MomentaTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedCallToAction(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MomentaLargeShape)
            .clickable(onClick = onClick),
        color = MomentaSurfaceAlt,
        shape = MomentaLargeShape,
        border = BorderStroke(1.dp, MomentaTextSecondary.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MomentaGreen.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = MomentaGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Смотреть мир сейчас",
                color = MomentaTextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MomentaTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EmptyBestMoment(onCaptureClick: () -> Unit) {
    MomentaCard(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MomentaGreen.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MomentaWarm)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Сегодняшний лучший момент еще впереди",
                color = MomentaText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Сделай снимок и задай тон этому дню.",
                color = MomentaTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            MomentaPrimaryButton(
                text = stringResource(R.string.capture_moment),
                onClick = onCaptureClick,
                height = 46.dp
            )
        }
    }
}

@Composable
private fun SmallErrorCard(title: String, message: String, action: String, onAction: () -> Unit) {
    MomentaCard(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = MomentaText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = MomentaTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            MomentaSecondaryButton(text = action, onClick = onAction)
        }
    }
}
