package com.bghitech.momenta.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppLatestDto(
    @SerialName("app_name") val appName: String,
    @SerialName("package_name") val packageName: String,
    val platform: String,
    val channel: String,
    @SerialName("version_name") val versionName: String,
    @SerialName("version_code") val versionCode: Int,
    @SerialName("min_supported_version_code") val minSupportedVersionCode: Int,
    val mandatory: Boolean,
    @SerialName("apk_url") val apkUrl: String,
    @SerialName("apk_sha256") val apkSha256: String? = null,
    @SerialName("apk_size_bytes") val apkSizeBytes: Long? = null,
    @SerialName("release_url") val releaseUrl: String? = null,
    @SerialName("release_notes") val releaseNotes: List<String> = emptyList(),
    @SerialName("published_at") val publishedAt: String
)
