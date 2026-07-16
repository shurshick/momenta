package com.bghitech.momenta.feature.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bghitech.momenta.BuildConfig
import com.bghitech.momenta.core.design.MomentaCard
import com.bghitech.momenta.core.design.MomentaDivider
import com.bghitech.momenta.core.design.MomentaError
import com.bghitech.momenta.core.design.MomentaGreen
import com.bghitech.momenta.core.design.MomentaLogoMark
import com.bghitech.momenta.core.design.MomentaScreen
import com.bghitech.momenta.core.design.MomentaSurface
import com.bghitech.momenta.core.design.MomentaText
import com.bghitech.momenta.core.design.MomentaTextSecondary
import com.bghitech.momenta.data.local.entity.UploadQueueEntity
import com.bghitech.momenta.feature.updates.AppUpdateInfo
import com.bghitech.momenta.feature.updates.AppUpdateViewModel
import com.bghitech.momenta.feature.updates.downloadAndOpenAppApk
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.launch

private const val SUPPORT_URL = "https://github.com/shurshick/momenta/issues/new"
private const val PRIVACY_URL = "https://github.com/shurshick/momenta/blob/master/docs/PRIVACY_POLICY.md"
private const val TERMS_URL = "https://github.com/shurshick/momenta/blob/master/docs/TERMS.md"

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    updateViewModel: AppUpdateViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var uploadToDelete by remember { mutableStateOf<UploadQueueEntity?>(null) }
    var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var isDownloadingApk by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = "Выйти",
            text = "Сессия на этом устройстве завершится.",
            confirmText = "Выйти",
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                scope.launch {
                    viewModel.logout()
                    onLogout()
                }
            }
        )
    }

    uploadToDelete?.let { upload ->
        ConfirmDialog(
            title = "Удалить из очереди?",
            text = "Неотправленная публикация будет удалена. Исходное фото в галерее останется.",
            confirmText = "Удалить",
            onDismiss = { uploadToDelete = null },
            onConfirm = {
                uploadToDelete = null
                viewModel.deleteUpload(upload.localId)
            }
        )
    }

    MomentaScreen(modifier = Modifier.statusBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            SettingsHeader(onBack)

            SettingsSection("Приложение") {
                AppInfoHeader()
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.Filled.SystemUpdate,
                    title = "Обновления",
                    subtitle = updateInfo?.message ?: "Версия ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    onClick = {
                        if (!isCheckingUpdate) {
                            scope.launch {
                                isCheckingUpdate = true
                                updateInfo = updateViewModel.checkLatestAppRelease()
                                isCheckingUpdate = false
                            }
                        }
                    },
                    trailing = {
                        val downloadUrl = updateInfo?.downloadUrl
                        if (updateInfo?.hasUpdate == true && downloadUrl != null) {
                            TextButton(
                                enabled = !isDownloadingApk,
                                onClick = {
                                    scope.launch {
                                        isDownloadingApk = true
                                        val message = downloadAndOpenAppApk(context, downloadUrl, updateInfo?.apkSha256)
                                        updateInfo = updateInfo?.copy(message = message)
                                        isDownloadingApk = false
                                    }
                                }
                            ) {
                                Text(if (isDownloadingApk) "Скачиваем" else "Скачать", color = MomentaGreen)
                            }
                        } else {
                            Text(
                                if (isCheckingUpdate) "Проверяем" else "Проверить",
                                color = MomentaGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                )
            }

            SettingsGap()
            SettingsSection("Публикации") {
                if (state.uploads.isEmpty()) {
                    SettingsActionRow(
                        icon = Icons.Filled.Upload,
                        title = "Очередь пуста",
                        subtitle = "Все публикации отправлены",
                        onClick = null
                    )
                } else {
                    state.uploads.forEachIndexed { index, upload ->
                        UploadQueueRow(
                            upload = upload,
                            onRetry = { viewModel.retryUpload(upload.localId) },
                            onDelete = { uploadToDelete = upload }
                        )
                        if (index != state.uploads.lastIndex) SettingsDivider()
                    }
                }
            }

            SettingsGap()
            SettingsSection("Хранилище") {
                SettingsActionRow(
                    icon = Icons.Filled.Storage,
                    title = "Кэш",
                    subtitle = "Временные фото и данные: ${formatBytes(state.cacheSizeBytes)}",
                    onClick = { viewModel.clearCache() },
                    trailing = {
                        Text(
                            if (state.isClearingCache) "Очищаем" else "Очистить",
                            color = MomentaGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }

            SettingsGap()
            SettingsSection("Аккаунт") {
                SettingsActionRow(
                    icon = Icons.Filled.Edit,
                    title = "Редактировать профиль",
                    subtitle = "Имя и описание",
                    onClick = onEditProfile
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Выйти",
                    subtitle = "Завершить сессию на этом устройстве",
                    tint = MomentaError,
                    onClick = { showLogoutDialog = true }
                )
            }

            SettingsGap()
            SettingsSection("Помощь") {
                SettingsActionRow(
                    icon = Icons.Filled.ContentCopy,
                    title = "Скопировать диагностику",
                    subtitle = "Версия, устройство, очередь и размер кэша",
                    onClick = {
                        copyDiagnostics(context, viewModel.diagnosticsText())
                        Toast.makeText(context, "Диагностика скопирована", Toast.LENGTH_SHORT).show()
                    }
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.Filled.BugReport,
                    title = "Сообщить о проблеме",
                    subtitle = "Открыть форму поддержки",
                    onClick = { openUrl(context, SUPPORT_URL) }
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.Filled.Policy,
                    title = "Конфиденциальность",
                    subtitle = "Какие данные использует приложение",
                    onClick = { openUrl(context, PRIVACY_URL) }
                )
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.Filled.Description,
                    title = "Условия использования",
                    subtitle = "Правила сервиса",
                    onClick = { openUrl(context, TERMS_URL) }
                )
            }

            Spacer(Modifier.height(28.dp))
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = MomentaText)
        }
        Text("Настройки", color = MomentaText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title,
        color = MomentaTextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    MomentaCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
        Column(content = content)
    }
}

@Composable
private fun AppInfoHeader() {
    Row(
        modifier = Modifier.padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MomentaLogoMark(size = 42)
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Момент", color = MomentaText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Один момент. Все вместе.", color = MomentaTextSecondary, fontSize = 12.sp)
            Text("© 2026 Александр Коваленко", color = MomentaTextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
    tint: Color = MomentaGreen,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = if (tint == MomentaError) MomentaError else MomentaText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                color = MomentaTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(Icons.Filled.ChevronRight, null, tint = MomentaTextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun UploadQueueRow(
    upload: UploadQueueEntity,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Upload, null, tint = uploadStatusColor(upload.status), modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                upload.caption?.takeIf { it.isNotBlank() } ?: "Публикация без подписи",
                color = MomentaText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${uploadStatusText(upload)} · ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(upload.createdAt))}",
                color = MomentaTextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onRetry, enabled = upload.status != "uploading") {
            Icon(Icons.Filled.Refresh, "Повторить", tint = if (upload.status == "uploading") MomentaTextSecondary else MomentaGreen)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.DeleteOutline, "Удалить", tint = MomentaError)
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = MomentaText) },
        text = { Text(text, color = MomentaTextSecondary) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText, color = MomentaError) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = MomentaGreen) } },
        containerColor = MomentaSurface
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = MomentaDivider)
}

@Composable
private fun SettingsGap() {
    Spacer(Modifier.height(18.dp))
}

private fun uploadStatusText(upload: UploadQueueEntity): String = when (upload.status) {
    "uploading" -> "Загружается"
    "failed" -> "Ошибка, попыток: ${upload.retryCount}"
    else -> "Ожидает подключения"
}

private fun uploadStatusColor(status: String): Color = when (status) {
    "failed" -> MomentaError
    "uploading" -> MomentaGreen
    else -> MomentaTextSecondary
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L * 1024L -> "%.1f ГБ".format(bytes / (1024.0 * 1024.0 * 1024.0))
    bytes >= 1024L * 1024L -> "%.1f МБ".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> "%.1f КБ".format(bytes / 1024.0)
    else -> "$bytes Б"
}

private fun copyDiagnostics(context: Context, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("Диагностика Момент", text))
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.onFailure {
        Toast.makeText(context, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
    }
}
