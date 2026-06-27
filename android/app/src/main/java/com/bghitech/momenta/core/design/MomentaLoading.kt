package com.bghitech.momenta.core.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun MomentaLoading(
    modifier: Modifier = Modifier,
    message: String = "Загрузка…"
) {
    val transition = rememberInfiniteTransition(label = "momenta-loading")
    val sweep = transition.animateFloat(
        initialValue = 210f,
        targetValue = 310f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Reverse),
        label = "loading-sweep"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(64.dp)) {
                val stroke = 4.dp.toPx()
                drawArc(
                    color = MomentaGreen,
                    startAngle = -40f,
                    sweepAngle = sweep.value,
                    useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = Size(size.width - stroke * 2, size.height - stroke * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawCircle(
                    color = MomentaWarm,
                    radius = 5.dp.toPx(),
                    center = Offset(size.width * 0.72f, size.height * 0.34f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = MomentaTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
