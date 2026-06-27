package com.bghitech.momenta.feature.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import android.widget.Toast
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
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
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
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var isDownloadingApk by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

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
                    updateInfo?.let {
                        Text(it.message, color = MomentaText, fontSize = 13.sp)
                        val downloadUrl = it.downloadUrl
                        if (it.hasUpdate && downloadUrl != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            MomentaPrimaryButton(
                                text = if (isDownloadingApk) "Скачиваем..." else "Скачать APK",
                                onClick = {
                                    if (!isDownloadingApk) {
                                        scope.launch {
                                            isDownloadingApk = true
                                            val message = downloadAndOpenApk(context, downloadUrl)
                                            updateInfo = it.copy(message = message)
                                            isDownloadingApk = false
                                        }
                                    }
                                },
                                enabled = !isDownloadingApk
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isCheckingUpdate,
                    onClick = {
                        scope.launch {
                            isCheckingUpdate = true
                            updateInfo = UpdateInfo("Проверяем обновление...")
                            updateInfo = checkLatestRelease()
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

private data class UpdateInfo(
    val message: String,
    val hasUpdate: Boolean = false,
    val downloadUrl: String? = null
)

private suspend fun checkLatestRelease(): UpdateInfo = withContext(Dispatchers.IO) {
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
        val release = JSONObject(json)
        val latestTag = release.optString("tag_name")
        val latest = latestTag.removePrefix("v")
        val releaseUrl = release.optString("html_url").ifBlank {
            "https://github.com/shurshick/momenta/releases/latest"
        }
        val assets = release.optJSONArray("assets")
        val apkUrl = (0 until (assets?.length() ?: 0))
            .asSequence()
            .mapNotNull { assets?.optJSONObject(it) }
            .firstOrNull { it.optString("name").endsWith(".apk") }
            ?.optString("browser_download_url")
            ?.takeIf { it.isNotBlank() }
            ?: releaseUrl
        if (latest.isBlank()) {
            UpdateInfo("Не удалось определить последнюю версию.")
        } else if (latest == BuildConfig.VERSION_NAME) {
            UpdateInfo("Установлена актуальная версия $latest.")
        } else {
            UpdateInfo(
                message = "Доступна версия $latest. Текущая: ${BuildConfig.VERSION_NAME}.",
                hasUpdate = true,
                downloadUrl = apkUrl
            )
        }
    }.getOrElse {
        UpdateInfo("Не удалось проверить обновление. Проверь интернет и повтори.")
    }
}

private suspend fun downloadAndOpenApk(context: Context, downloadUrl: String): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
        val settingsIntent = Intent(
            AndroidSettings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(settingsIntent)
        return "Разреши установку из этого источника, затем нажми «Скачать APK» еще раз."
    }

    return runCatching {
        val apkFile = withContext(Dispatchers.IO) {
            val output = File(context.externalCacheDir ?: context.cacheDir, "momenta-update.apk")
            URL(downloadUrl).openConnection().apply {
                connectTimeout = 15000
                readTimeout = 30000
                setRequestProperty("User-Agent", "Momenta/${BuildConfig.VERSION_NAME}")
            }.getInputStream().use { input ->
                output.outputStream().use { outputStream ->
                    input.copyTo(outputStream)
                }
            }
            output
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(installIntent)
        "APK скачан. Подтверди установку в системном окне."
    }.getOrElse { error ->
        if (error is ActivityNotFoundException) {
            Toast.makeText(context, "Не найден системный установщик APK", Toast.LENGTH_LONG).show()
            "Не найден системный установщик APK."
        } else {
            "Не удалось скачать APK. Проверь интернет и повтори."
        }
    }
}
