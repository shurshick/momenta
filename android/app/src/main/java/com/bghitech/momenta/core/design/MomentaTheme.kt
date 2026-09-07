package com.bghitech.momenta.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = MomentaBrandGreenDark,
    onPrimary = MomentaDarkBackground,
    primaryContainer = Color(0xFF0C4B31),
    onPrimaryContainer = MomentaDarkText,
    secondary = MomentaBrandWarm,
    onSecondary = MomentaDarkBackground,
    tertiary = Color(0xFF70B5FF),
    background = MomentaDarkBackground,
    onBackground = MomentaDarkText,
    surface = MomentaDarkSurface,
    onSurface = MomentaDarkText,
    surfaceVariant = MomentaDarkSurfaceAlt,
    onSurfaceVariant = MomentaDarkTextSecondary,
    outline = Color(0xFF69736D),
    outlineVariant = MomentaDarkDivider,
    error = Color(0xFFFF6B83),
    onError = MomentaDarkText
)

private val LightColors = lightColorScheme(
    primary = MomentaBrandGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F7DD),
    onPrimaryContainer = Color(0xFF063D26),
    secondary = Color(0xFFE39725),
    onSecondary = MomentaLightText,
    tertiary = MomentaBrandBlue,
    background = MomentaLightBackground,
    onBackground = MomentaLightText,
    surface = MomentaLightSurface,
    onSurface = MomentaLightText,
    surfaceVariant = MomentaLightSurfaceAlt,
    onSurfaceVariant = MomentaLightTextSecondary,
    outline = Color(0xFF748078),
    outlineVariant = MomentaLightDivider,
    error = MomentaBrandError,
    onError = Color.White
)

@Composable
fun MomentaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MomentaTypography,
        shapes = MomentaMaterialShapes,
        content = content
    )
}
