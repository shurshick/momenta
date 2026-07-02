package com.bghitech.momenta.feature.updates

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bghitech.momenta.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL

data class AppUpdateInfo(
    val message: String,
    val hasUpdate: Boolean = false,
    val downloadUrl: String? = null,
    val latestVersion: String? = null
)

suspend fun checkLatestAppRelease(): AppUpdateInfo = withContext(Dispatchers.IO) {
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

        when {
            latest.isBlank() -> AppUpdateInfo("Не удалось определить последнюю версию.")
            !isRemoteVersionNewer(latest, BuildConfig.VERSION_NAME) -> {
                AppUpdateInfo("Установлена актуальная версия $latest.", latestVersion = latest)
            }
            else -> AppUpdateInfo(
                message = "Доступна версия $latest. Сейчас установлена ${BuildConfig.VERSION_NAME}.",
                hasUpdate = true,
                downloadUrl = apkUrl,
                latestVersion = latest
            )
        }
    }.getOrElse {
        AppUpdateInfo("Не удалось проверить обновление. Проверь интернет и повтори.")
    }
}

suspend fun downloadAndOpenAppApk(context: Context, downloadUrl: String): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
        val settingsIntent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
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

private fun isRemoteVersionNewer(remote: String, current: String): Boolean {
    val remoteParts = remote.split(".", "-").map { it.toIntOrNull() ?: 0 }
    val currentParts = current.split(".", "-").map { it.toIntOrNull() ?: 0 }
    val maxSize = maxOf(remoteParts.size, currentParts.size)
    for (index in 0 until maxSize) {
        val remotePart = remoteParts.getOrElse(index) { 0 }
        val currentPart = currentParts.getOrElse(index) { 0 }
        if (remotePart != currentPart) return remotePart > currentPart
    }
    return false
}
