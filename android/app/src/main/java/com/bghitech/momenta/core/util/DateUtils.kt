package com.bghitech.momenta.core.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatRelativeTime(isoDate: String): String {
        return try {
            val date = parseIsoDate(isoDate) ?: return isoDate
            val now = Date()
            val diff = (now.time - date.time).coerceAtLeast(0L)

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            when {
                days > 7 -> "${days / 7} нед."
                days > 0 -> "$days д."
                hours > 0 -> "$hours ч"
                minutes > 0 -> "$minutes мин"
                else -> "только что"
            }
        } catch (_: Exception) {
            isoDate
        }
    }

    private fun parseIsoDate(value: String): Date? {
        val normalized = normalizeIsoFraction(value)
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss"
        )
        for (pattern in patterns) {
            runCatching {
                val format = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                return format.parse(normalized)
            }
        }
        return null
    }

    private fun normalizeIsoFraction(value: String): String =
        value.replace(Regex("""\.(\d{1,9})(Z|[+-]\d{2}:?\d{2})?$""")) { match ->
            val millis = match.groupValues[1].padEnd(3, '0').take(3)
            val zone = match.groupValues.getOrNull(2).orEmpty()
            ".$millis$zone"
        }
}
