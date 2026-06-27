package com.bghitech.momenta.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bghitech.momenta.core.design.*
import com.bghitech.momenta.BuildConfig
import com.bghitech.momenta.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf(BuildConfig.DEFAULT_SERVER_URL) }
    var showConnectionResult by remember { mutableStateOf<String?>(null) }
    var updateStatus by remember { mutableStateOf<String?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = {
                Icon(Icons.Default.Info, contentDescription = null, tint = MomentaGreen)
            },
            title = { Text("О программе", color = MomentaText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Момента", color = MomentaText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Один момент. Все вместе.", color = MomentaTextSecondary, fontSize = 13.sp)
                    Text("Версия ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", color = MomentaTextSecondary, fontSize = 13.sp)
                    Text("© 2026 BGHitech / shurshick", color = MomentaTextSecondary, fontSize = 13.sp)
                    updateStatus?.let {
                        Text(it, color = MomentaText, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isCheckingUpdate,
                    onClick = {
                        scope.launch {
                            isCheckingUpdate = true
                            updateStatus = "Проверяем обновление..."
                            updateStatus = checkLatestRelease()
                            isCheckingUpdate = false
                        }
                    }
                ) {
                    Text(if (isCheckingUpdate) "Проверяем..." else "Проверить обновление", color = MomentaGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Закрыть", color = MomentaTextSecondary)
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
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = MomentaText)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Настройки",
                color = MomentaText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "Сервер") {
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("API URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MomentaText,
                    unfocusedTextColor = MomentaText,
                    cursorColor = MomentaGreen,
                    focusedBorderColor = MomentaGreen,
                    unfocusedBorderColor = MomentaDivider,
                    focusedLabelColor = MomentaGreen,
                    unfocusedLabelColor = MomentaTextSecondary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            MomentaSecondaryButton(
                text = "Проверить соединение",
                onClick = { showConnectionResult = "Проверка... (реализовано в v0.2.0)" }
            )
        }

        if (showConnectionResult != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = showConnectionResult!!,
                color = MomentaTextSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = "Аккаунт") {
            SettingsToggleRow("Видимость профиля", "Ваш профиль виден всем")
            SettingsToggleRow("Геолокация на фото", "Добавлять страну и город")
            SettingsToggleRow("Push-уведомления", "О лайках и новых моментах")
        }

        Spacer(modifier = Modifier.height(32.dp))

        SettingsSection(title = "Приложение") {
            SettingsInfoRow("Версия", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            SettingsInfoRow("Flavor", BuildConfig.FLAVOR)
            SettingsInfoRow("API URL", BuildConfig.DEFAULT_SERVER_URL)
            SettingsInfoRow("Media URL", BuildConfig.MEDIA_BASE_URL)
            SettingsInfoRow("Логирование", if (BuildConfig.LOGGING_ENABLED) "Включено" else "Отключено")
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { showAboutDialog = true },
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MomentaGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("О программе", color = MomentaGreen)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        MomentaSecondaryButton(
            text = "Выйти",
            onClick = { showLogoutDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        color = MomentaTextSecondary,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    MomentaCard {
        Column(content = content)
    }
}

@Composable
private fun SettingsToggleRow(label: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = MomentaText, fontSize = 14.sp)
            Text(text = description, color = MomentaTextSecondary, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = true,
            onCheckedChange = { },
            colors = SwitchDefaults.colors(checkedTrackColor = MomentaGreen, uncheckedTrackColor = MomentaSurfaceAlt)
        )
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MomentaTextSecondary, fontSize = 13.sp)
        Text(text = value, color = MomentaText, fontSize = 13.sp)
    }
}

private suspend fun checkLatestRelease(): String = withContext(Dispatchers.IO) {
    runCatching {
        val json = URL("https://api.github.com/repos/shurshick/momenta/releases/latest")
            .openConnection()
            .apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
            }
            .getInputStream()
            .bufferedReader()
            .use { it.readText() }
        val latest = JSONObject(json).optString("tag_name").removePrefix("v")
        if (latest.isBlank()) {
            "Не удалось определить последнюю версию."
        } else if (latest == BuildConfig.VERSION_NAME) {
            "Установлена актуальная версия $latest."
        } else {
            "Доступна версия $latest. Текущая: ${BuildConfig.VERSION_NAME}."
        }
    }.getOrElse {
        "Не удалось проверить обновление. Проверь интернет и повтори."
    }
}
