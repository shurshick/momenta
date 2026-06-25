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
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                val endTime = format.parse(endsAtIso)?.time ?: 0L
                val now = System.currentTimeMillis()
                val diff = endTime - now

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
