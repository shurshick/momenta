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
import com.bghitech.momenta.data.remote.MomentaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.security.MessageDigest

data class AppUpdateInfo(
    val message: String,
    val hasUpdate: Boolean = false,
    val downloadUrl: String? = null,
    val latestVersion: String? = null,
    val latestVersionCode: Int? = null,
    val mandatory: Boolean = false,
    val apkSha256: String? = null
)

suspend fun checkLatestAppRelease(api: MomentaApi): AppUpdateInfo = withContext(Dispatchers.IO) {
    runCatching {
        val latest = api.getLatestApp()
        val isCurrentPackage = BuildConfig.APPLICATION_ID == latest.packageName ||
            BuildConfig.APPLICATION_ID.startsWith("${latest.packageName}.")

        when {
            latest.platform.lowercase() != "android" || !isCurrentPackage -> {
                AppUpdateInfo("Сервер вернул обновление не для этой сборки.")
            }
            latest.versionCode <= BuildConfig.VERSION_CODE -> {
                AppUpdateInfo(
                    message = "Установлена актуальная версия ${BuildConfig.VERSION_NAME}.",
                    latestVersion = latest.versionName,
                    latestVersionCode = latest.versionCode
                )
            }
            latest.apkUrl.isBlank() -> {
                AppUpdateInfo("Новая версия найдена, но ссылка на APK пока не задана.")
            }
            else -> {
                val prefix = if (latest.mandatory) "Доступно важное обновление" else "Доступна версия"
                AppUpdateInfo(
                    message = "$prefix ${latest.versionName}. Сейчас установлена ${BuildConfig.VERSION_NAME}.",
                    hasUpdate = true,
                    downloadUrl = latest.apkUrl,
                    latestVersion = latest.versionName,
                    latestVersionCode = latest.versionCode,
                    mandatory = latest.mandatory || BuildConfig.VERSION_CODE < latest.minSupportedVersionCode,
                    apkSha256 = latest.apkSha256?.takeIf { it.isNotBlank() }
                )
            }
        }
    }.getOrElse {
        AppUpdateInfo("Не удалось проверить обновление. Проверь интернет и повтори.")
    }
}

suspend fun downloadAndOpenAppApk(
    context: Context,
    downloadUrl: String,
    expectedSha256: String? = null
): String {
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

            val normalizedExpectedHash = expectedSha256
                ?.removePrefix("sha256:")
                ?.trim()
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }
            if (normalizedExpectedHash != null) {
                val actualHash = output.sha256()
                if (actualHash != normalizedExpectedHash) {
                    output.delete()
                    error("APK checksum mismatch")
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
        } else if (error.message == "APK checksum mismatch") {
            "Файл обновления поврежден. Попробуй скачать позже."
        } else {
            "Не удалось скачать APK. Проверь интернет и повтори."
        }
    }
}

private fun File.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
}
