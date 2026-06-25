package com.bghitech.momenta.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MomentaDarkColorScheme = darkColorScheme(
    primary = MomentaGreen,
    onPrimary = MomentaBackground,
    primaryContainer = MomentaGreenAlpha,
    secondary = MomentaWarm,
    onSecondary = MomentaBackground,
    tertiary = MomentaBlue,
    background = MomentaBackground,
    onBackground = MomentaText,
    surface = MomentaSurface,
    onSurface = MomentaText,
    surfaceVariant = MomentaSurfaceAlt,
    onSurfaceVariant = MomentaTextSecondary,
    outline = MomentaDivider,
    error = MomentaError,
    onError = MomentaText
)

@Composable
fun MomentaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MomentaDarkColorScheme,
        typography = MomentaTypography,
        content = content
    )
}
