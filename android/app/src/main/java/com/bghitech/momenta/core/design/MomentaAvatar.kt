package com.bghitech.momenta.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

private val AvatarPalette = listOf(
    Color(0xFF29F08A), Color(0xFFFFC46B), Color(0xFF5CA8FF), Color(0xFFFF6B9E),
    Color(0xFF9B7CFF), Color(0xFF45E0D0), Color(0xFFFF8A4C), Color(0xFFB8F15A),
    Color(0xFF6FD2FF), Color(0xFFFFD166), Color(0xFFEF476F), Color(0xFF06D6A0),
    Color(0xFF118AB2), Color(0xFFE9C46A), Color(0xFFF4A261), Color(0xFF2A9D8F),
    Color(0xFFE76F51), Color(0xFF8ECAE6), Color(0xFFB5179E), Color(0xFF80ED99)
)

@Composable
fun MomentaAvatar(
    avatarUrl: String?,
    avatarKey: String?,
    username: String?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    if (!avatarUrl.isNullOrBlank()) {
        Image(
            painter = rememberAsyncImagePainter(avatarUrl),
            contentDescription = null,
            modifier = modifier.size(size).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        return
    }

    val index = remember(avatarKey) {
        avatarKey?.substringAfterLast("_")?.toIntOrNull()?.minus(1)?.coerceIn(0, 19) ?: 0
    }
    val color = AvatarPalette[index]
    val secondary = AvatarPalette[(index + 7) % AvatarPalette.size]
    val initial = username?.firstOrNull()?.uppercaseChar()?.toString() ?: "M"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(color.copy(alpha = 0.9f), MomentaSurfaceAlt))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.toPx() * 0.06f
            drawCircle(
                color = secondary.copy(alpha = 0.85f),
                radius = this.size.minDimension * 0.36f,
                style = Stroke(width = strokeWidth)
            )
            drawCircle(
                color = MomentaWarm,
                radius = this.size.minDimension * 0.11f,
                center = Offset(this.size.width * (0.64f + (index % 3) * 0.04f), this.size.height * 0.36f)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.24f),
                radius = this.size.minDimension * 0.38f,
                center = Offset(this.size.width * 0.46f, this.size.height * 0.54f)
            )
        }
        Text(
            text = initial,
            color = MomentaText,
            fontSize = (size.value * 0.36f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
