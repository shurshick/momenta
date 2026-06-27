package com.bghitech.momenta.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bghitech.momenta.R
import com.bghitech.momenta.core.design.MomentaBlue
import com.bghitech.momenta.core.design.MomentaCard
import com.bghitech.momenta.core.design.MomentaGreen
import com.bghitech.momenta.core.design.MomentaLargeShape
import com.bghitech.momenta.core.design.MomentaLoading
import com.bghitech.momenta.core.design.MomentaPrimaryButton
import com.bghitech.momenta.core.design.MomentaScreen
import com.bghitech.momenta.core.design.MomentaSecondaryButton
import com.bghitech.momenta.core.design.MomentaSurfaceAlt
import com.bghitech.momenta.core.design.MomentaText
import com.bghitech.momenta.core.design.MomentaTextSecondary
import com.bghitech.momenta.core.design.MomentaWarm
import com.bghitech.momenta.core.design.rememberCountdownTime

@Composable
fun TodayScreen(
    onCaptureClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MomentaScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.today_title),
                        color = MomentaText,
                        fontSize = 24.sp,
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

            when {
                state.isLoading && state.challenge == null -> MomentaLoading(
                    message = "Загружаем момент…"
                )

                state.error != null && !state.isOffline -> ErrorState(
                    message = state.error!!,
                    onRetry = { viewModel.loadChallenge() }
                )

                state.challenge != null -> {
                    val challenge = state.challenge!!
                    MomentaCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.daily_topic_label),
                                    color = MomentaGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${challenge.participantsCount} участвуют",
                                    color = MomentaTextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = challenge.title.ifBlank {
                                    stringResource(R.string.default_challenge_title)
                                },
                                color = MomentaText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 34.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = challenge.description
                                    ?: stringResource(R.string.default_challenge_description),
                                color = MomentaTextSecondary,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val timeLeft = if (challenge.endsAt != null) {
                                rememberCountdownTime(challenge.endsAt)
                            } else {
                                ""
                            }
                            if (timeLeft.isNotBlank()) {
                                Text(
                                    text = "До конца дня $timeLeft",
                                    color = MomentaWarm,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            if (challenge.userPosted) {
                                Text(
                                    text = stringResource(R.string.already_posted),
                                    color = MomentaText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                MomentaSecondaryButton(
                                    text = stringResource(R.string.watch_world_now),
                                    onClick = { }
                                )
                            } else {
                                MomentaPrimaryButton(
                                    text = stringResource(R.string.join_today),
                                    onClick = onCaptureClick
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.examples_title),
                        color = MomentaText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(MomentaWarm, MomentaGreen, MomentaBlue).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.72f)
                                    .clip(MomentaLargeShape)
                                    .background(MomentaSurfaceAlt),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
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
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            MomentaPrimaryButton(text = stringResource(R.string.retry), onClick = onRetry)
        }
    }
}
