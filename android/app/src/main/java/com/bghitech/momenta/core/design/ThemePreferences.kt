package com.bghitech.momenta.core.design

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MomentaThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorage(value: String?): MomentaThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}

object ThemePreferences {
    private const val PREFERENCES = "momenta_appearance"
    private const val KEY_THEME = "theme"

    private lateinit var appContext: Context
    private val mutableMode = MutableStateFlow(MomentaThemeMode.SYSTEM)
    val mode: StateFlow<MomentaThemeMode> = mutableMode.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val stored = appContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(KEY_THEME, null)
        mutableMode.value = MomentaThemeMode.fromStorage(stored)
    }

    fun setMode(mode: MomentaThemeMode) {
        check(::appContext.isInitialized) { "ThemePreferences is not initialized" }
        mutableMode.value = mode
        appContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, mode.storageValue)
            .apply()
    }
}
