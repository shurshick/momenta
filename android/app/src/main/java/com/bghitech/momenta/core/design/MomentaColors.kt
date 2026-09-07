package com.bghitech.momenta.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

internal val MomentaDarkBackground = Color(0xFF0B0E12)
internal val MomentaDarkSurface = Color(0xFF141820)
internal val MomentaDarkSurfaceAlt = Color(0xFF1C222D)
internal val MomentaDarkText = Color(0xFFF4F7F5)
internal val MomentaDarkTextSecondary = Color(0xFFA8B0B8)
internal val MomentaDarkDivider = Color(0xFF29313A)

internal val MomentaLightBackground = Color(0xFFF7F9F7)
internal val MomentaLightSurface = Color(0xFFFFFFFF)
internal val MomentaLightSurfaceAlt = Color(0xFFEAF0EC)
internal val MomentaLightText = Color(0xFF151A17)
internal val MomentaLightTextSecondary = Color(0xFF5D6861)
internal val MomentaLightDivider = Color(0xFFD9E1DC)

internal val MomentaBrandGreen = Color(0xFF14C978)
internal val MomentaBrandGreenDark = Color(0xFF29F08A)
internal val MomentaBrandWarm = Color(0xFFFFB84D)
internal val MomentaBrandBlue = Color(0xFF3F8FE8)
internal val MomentaBrandError = Color(0xFFE54864)

val MomentaBackground: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
val MomentaSurface: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
val MomentaSurfaceAlt: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant
val MomentaGreen: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val MomentaWarm: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondary
val MomentaBlue: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiary
val MomentaText: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onBackground
val MomentaTextSecondary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
val MomentaDivider: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
val MomentaError: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.error
val MomentaGreenAlpha: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
val MomentaGreenGlow: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
val MomentaCardBorder: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
