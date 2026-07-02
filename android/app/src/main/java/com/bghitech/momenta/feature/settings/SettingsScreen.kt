package com.bghitech.momenta.feature.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bghitech.momenta.BuildConfig
import com.bghitech.momenta.core.design.MomentaCard
import com.bghitech.momenta.core.design.MomentaDivider
import com.bghitech.momenta.core.design.MomentaError
import com.bghitech.momenta.core.design.MomentaGreen
import com.bghitech.momenta.core.design.MomentaLargeShape
import com.bghitech.momenta.core.design.MomentaLogoMark
import com.bghitech.momenta.core.design.MomentaPrimaryButton
import com.bghitech.momenta.core.design.MomentaScreen
import com.bghitech.momenta.core.design.MomentaSecondaryButton
import com.bghitech.momenta.core.design.MomentaSurface
import com.bghitech.momenta.core.design.MomentaSurfaceAlt
import com.bghitech.momenta.core.design.MomentaText
import com.bghitech.momenta.core.design.MomentaTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var isDownloadingApk by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выйти", color = MomentaText) },
            text = { Text("Сессия завершится, локальные токены будут удалены.", color = MomentaTextSecondary) },
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

    MomentaScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            SettingsHeader(onBack = onBack)

            SettingsSection(title = "Сервер") {
                OutlinedTextField(
                    value = state.serverUrlInput,
                    onValueChange = viewModel::onServerUrlChange,
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
                MomentaPrimaryButton(
                    text = if (state.isCheckingConnection) "Проверяем..." else "Сохранить и проверить",
                    onClick = viewModel::saveAndCheckServer,
                    enabled = !state.isCheckingConnection,
                    loading = state.isCheckingConnection
                )
                state.connectionMessage?.let { message ->
                    Spacer(modifier = Modifier.height(10.dp))
                    StatusLine(message = message, positive = message.contains("работает"))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            SettingsSection(title = "Приложение") {
                AppInfoHeader()
                Spacer(modifier = Modifier.height(14.dp))
                SettingsInfoRow("Версия", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                SettingsInfoRow("Сборка", BuildConfig.FLAVOR)
                SettingsInfoRow("API", state.savedServerUrl)
                SettingsInfoRow("Медиа", BuildConfig.MEDIA_BASE_URL)
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSwitchRow(
                    title = "Логирование",
                    description = "Подробные сетевые логи для отладки",
                    checked = state.loggingEnabled,
                    onCheckedChange = viewModel::setLoggingEnabled
                )
                Spacer(modifier = Modifier.height(12.dp))
                UpdateBlock(
                    updateInfo = updateInfo,
                    isCheckingUpdate = isCheckingUpdate,
                    isDownloadingApk = isDownloadingApk,
                    onCheckUpdate = {
                        scope.launch {
                            isCheckingUpdate = true
                            updateInfo = UpdateInfo("Проверяем обновление...")
                            updateInfo = checkLatestRelease()
                            isCheckingUpdate = false
                        }
                    },
                    onDownload = { url ->
                        scope.launch {
                            isDownloadingApk = true
                            val message = downloadAndOpenApk(context, url)
                            updateInfo = updateInfo?.copy(message = message)
                            isDownloadingApk = false
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            SettingsSection(title = "Аккаунт") {
                Text(
                    text = "Выход завершит текущую сессию на этом устройстве.",
                    color = MomentaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                MomentaSecondaryButton(
                    text = "Выйти",
                    onClick = { showLogoutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = MomentaText)
        }
        Text(
            text = "Настройки",
            color = MomentaText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        color = MomentaTextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    MomentaCard(contentPadding = PaddingValues(16.dp)) {
        Column(content = content)
    }
}

@Composable
private fun AppInfoHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        MomentaLogoMark(size = 44)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Момент", color = MomentaText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Один момент. Все вместе.", color = MomentaTextSecondary, fontSize = 12.sp)
            Text("© 2026 BGHitech / shurshick", color = MomentaTextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, color = MomentaTextSecondary, fontSize = 13.sp, modifier = Modifier.width(68.dp))
        Text(
            text = value,
            color = MomentaText,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MomentaText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = MomentaTextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MomentaGreen,
                uncheckedTrackColor = MomentaSurfaceAlt
            )
        )
    }
}

@Composable
private fun UpdateBlock(
    updateInfo: UpdateInfo?,
    isCheckingUpdate: Boolean,
    isDownloadingApk: Boolean,
    onCheckUpdate: () -> Unit,
    onDownload: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MomentaLargeShape,
        color = MomentaSurfaceAlt.copy(alpha = 0.62f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.SettingsApplications, contentDescription = null, tint = MomentaGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Обновления", color = MomentaText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            updateInfo?.let {
                Text(it.message, color = MomentaTextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                Spacer(modifier = Modifier.height(10.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MomentaSecondaryButton(
                    text = if (isCheckingUpdate) "Проверяем..." else "Проверить",
                    onClick = onCheckUpdate,
                    enabled = !isCheckingUpdate,
                    modifier = Modifier.weight(1f),
                    height = 44.dp
                )
                val downloadUrl = updateInfo?.downloadUrl
                if (updateInfo?.hasUpdate == true && downloadUrl != null) {
                    MomentaPrimaryButton(
                        text = if (isDownloadingApk) "Скачиваем..." else "Скачать",
                        onClick = { onDownload(downloadUrl) },
                        enabled = !isDownloadingApk,
                        loading = isDownloadingApk,
                        modifier = Modifier.weight(1f),
                        height = 44.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusLine(message: String, positive: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (positive) Icons.Filled.CloudDone else Icons.Filled.Info,
            contentDescription = null,
            tint = if (positive) MomentaGreen else MomentaTextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            color = if (positive) MomentaGreen else MomentaTextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
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
                message = "Доступна версия $latest. Сейчас установлена ${BuildConfig.VERSION_NAME}.",
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
        return "Разреши установку из этого источника, затем нажми Скачать еще раз."
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
