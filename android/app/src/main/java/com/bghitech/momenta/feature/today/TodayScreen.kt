package com.bghitech.momenta.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import com.bghitech.momenta.core.design.*
import com.bghitech.momenta.domain.model.Post

@Composable
fun TodayScreen(
    onCaptureClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = MomentaText,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Момента дня",
                color = MomentaText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Настройки",
                    tint = MomentaTextSecondary
                )
            }
        }

        if (state.isLoading && state.challenge == null) {
            MomentaLoading(message = "Загружаем момент…")
            return
        }

        if (state.error != null && !state.isOffline) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.error!!,
                        color = MomentaError,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MomentaPrimaryButton(text = "Повторить", onClick = { viewModel.loadChallenge() })
                }
            }
            return
        }

        val challenge = state.challenge ?: return

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Твой день",
            color = MomentaGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = challenge.title,
            color = MomentaText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = challenge.description ?: "Замечай то, что делает твой день особенным.",
            color = MomentaTextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${challenge.participantsCount} участников",
                color = MomentaTextSecondary,
                fontSize = 13.sp
            )
            val timeLeft = if (challenge.endsAt != null) rememberCountdownTime(challenge.endsAt) else ""
            Text(
                text = timeLeft,
                color = MomentaWarm,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        MomentaPrimaryButton(
            text = "Принять участие",
            onClick = onCaptureClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Примеры моментов",
            color = MomentaTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(6) {
                Surface(
                    modifier = Modifier
                        .aspectRatio(0.75f)
                        .clip(MomentaMediumShape)
                        .clickable { },
                    color = MomentaSurfaceAlt
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("•", color = MomentaGreenAlpha, fontSize = 24.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
