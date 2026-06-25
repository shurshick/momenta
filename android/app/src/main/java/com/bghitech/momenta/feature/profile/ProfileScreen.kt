package com.bghitech.momenta.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bghitech.momenta.core.design.*

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

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
            return
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(MomentaRoundShape),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MomentaSurfaceAlt,
                shape = MomentaRoundShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("•", color = MomentaGreen, fontSize = 32.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = state.displayName,
            color = MomentaText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = state.username,
            color = MomentaTextSecondary,
            fontSize = 14.sp
        )

        if (state.bio != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.bio!!,
                color = MomentaTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "${state.momentsCount}", label = "моментов")
            StatItem(value = "${state.streakCount}", label = "дней подряд")
            StatItem(value = "${state.likesCount}", label = "лайков")
        }

        Spacer(modifier = Modifier.height(24.dp))

        MomentaSecondaryButton(
            text = "Редактировать",
            onClick = { }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Недавние моменты",
            color = MomentaTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(6) {
                Surface(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(MomentaMediumShape),
                    color = MomentaSurfaceAlt
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("•", color = MomentaGreenAlpha, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
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
            fontSize = 11.sp
        )
    }
}
