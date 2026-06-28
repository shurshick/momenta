package com.bghitech.momenta.feature.today

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import com.bghitech.momenta.core.design.MomentaCard
import com.bghitech.momenta.core.design.MomentaGreen
import com.bghitech.momenta.core.design.MomentaGreenAlpha
import com.bghitech.momenta.core.design.MomentaLargeShape
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
    onSettingsClick: () -> Unit,
    onOpenFeed: () -> Unit,
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
            Header(onSettingsClick = onSettingsClick)

            ChallengeSection(
                state = state,
                onRetry = viewModel::loadChallenge,
                onCaptureClick = onCaptureClick,
                onOpenFeed = onOpenFeed
            )

            Spacer(modifier = Modifier.height(12.dp))

            BestMomentSection(
                state = state,
                onCaptureClick = onCaptureClick,
                onOpenFeed = onOpenFeed
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun Header(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.today_title),
                color = MomentaText,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.slogan),
                color = MomentaTextSecondary,
                fontSize = 13.sp
            )
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = MomentaTextSecondary
            )
        }
    }
}

@Composable
private fun ChallengeSection(
    state: TodayUiState,
    onRetry: () -> Unit,
    onCaptureClick: () -> Unit,
    onOpenFeed: () -> Unit
) {
    when {
        state.challenge != null -> ChallengeCard(
            challenge = state.challenge,
            userPostedToday = state.userPostedToday || state.challenge.userPosted,
            onCaptureClick = onCaptureClick,
            onOpenFeed = onOpenFeed
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
    onCaptureClick: () -> Unit,
    onOpenFeed: () -> Unit
) {
    MomentaCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(MomentaGreen)
            )
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(MomentaLargeShape)
                        .background(MomentaGreenAlpha)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Задание дня",
                        color = MomentaGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${challenge.participantsCount} участвуют",
                    color = MomentaTextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = challenge.title.ifBlank { stringResource(R.string.default_challenge_title) },
                color = MomentaText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = challenge.prompt ?: challenge.description ?: stringResource(R.string.default_challenge_description),
                color = MomentaTextSecondary,
                fontSize = 12.sp,
                lineHeight = 15.sp
            )

            if (challenge.endsAt != null) {
                val timeLeft = rememberCountdownTime(challenge.endsAt)
                if (timeLeft.isNotBlank()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "До конца дня $timeLeft",
                        color = MomentaWarm,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (userPostedToday) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.already_posted),
                        color = MomentaText,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    MomentaSecondaryButton(
                        text = "Смотреть",
                        onClick = onOpenFeed,
                        modifier = Modifier.width(132.dp),
                        height = 40.dp
                    )
                }
            } else {
                MomentaPrimaryButton(
                    text = stringResource(R.string.capture_moment),
                    onClick = onCaptureClick,
                    height = 44.dp
                )
            }
            }
        }
    }
}

@Composable
private fun BestMomentSection(
    state: TodayUiState,
    onCaptureClick: () -> Unit,
    onOpenFeed: () -> Unit
) {
    Column {
        Text(
            text = "Лучший момент дня",
            color = MomentaText,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        when {
            state.bestPost != null -> BestMomentCard(post = state.bestPost, onClick = onOpenFeed)
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
        Column {
            Image(
                painter = rememberAsyncImagePainter(model = post.thumbUrl ?: post.previewUrl),
                contentDescription = "Лучший момент дня",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.18f)
                    .clip(MomentaLargeShape),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = post.user.displayName ?: "@${post.user.username}",
                        color = MomentaText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = MomentaWarm,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = post.likesCount.toString(),
                            color = MomentaText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (!post.caption.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = post.caption,
                        color = MomentaTextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }
            }
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
