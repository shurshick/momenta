package com.bghitech.momenta.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object AppDateUtils {
    private const val APP_TIMEZONE = "Europe/Moscow"

    fun todayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone(APP_TIMEZONE)
        }.format(Date())
    }
}
