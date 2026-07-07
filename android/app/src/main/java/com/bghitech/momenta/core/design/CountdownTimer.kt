package com.bghitech.momenta.core.design

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun rememberCountdownTime(endsAtIso: String): String {
    var timeLeft by remember { mutableStateOf("") }

    LaunchedEffect(endsAtIso) {
        while (true) {
            try {
                val endTime = parseIsoEndTime(endsAtIso)
                val now = System.currentTimeMillis()
                val diff = (endTime - now).coerceAtMost(24L * 60L * 60L * 1000L)

                if (diff <= 0) {
                    timeLeft = "00:00:00"
                    break
                }

                val hours = (diff / 3600000).toInt()
                val minutes = ((diff % 3600000) / 60000).toInt()
                val seconds = ((diff % 60000) / 1000).toInt()
                timeLeft = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } catch (_: Exception) {
                timeLeft = "--:--:--"
            }
            delay(1000)
        }
    }

    return timeLeft
}

private fun parseIsoEndTime(value: String): Long {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss"
    )
    for (pattern in patterns) {
        try {
            val format = SimpleDateFormat(pattern, Locale.US)
            if (!pattern.contains("X")) {
                format.timeZone = TimeZone.getTimeZone("UTC")
            }
            return format.parse(value)?.time ?: continue
        } catch (_: Exception) {
        }
    }
    return 0L
}
