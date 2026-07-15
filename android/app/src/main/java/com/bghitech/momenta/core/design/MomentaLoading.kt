package com.bghitech.momenta.core.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun MomentaLoading(
    modifier: Modifier = Modifier,
    message: String = "Загрузка…"
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MomentaLoadingMark(size = 64)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = MomentaTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun MomentaLoadingMark(
    modifier: Modifier = Modifier,
    size: Int = 40,
    rotating: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "momenta-loading-mark")
    val rotation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "momenta-loading-rotation"
    )
    MomentaLogoMark(
        size = size,
        modifier = modifier.graphicsLayer {
            rotationZ = if (rotating) rotation.value else 0f
        }
    )
}
