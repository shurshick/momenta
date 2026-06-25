package com.bghitech.momenta.core.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatRelativeTime(isoDate: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            val date = format.parse(isoDate) ?: return isoDate
            val now = Date()
            val diff = now.time - date.time

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
}
