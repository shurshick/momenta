package com.bghitech.momenta.core.common

import com.bghitech.momenta.BuildConfig

object MediaConfig {
    val baseUrl: String get() = BuildConfig.MEDIA_BASE_URL

    fun fullUrl(path: String): String {
        if (path.startsWith("http")) return path
        val base = baseUrl.trimEnd('/')
        return "$base/${path.trimStart('/')}"
    }
}
