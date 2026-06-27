package com.bghitech.momenta.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MomentaLogoMark(
    modifier: Modifier = Modifier,
    size: Int = 80
) {
    Canvas(modifier = modifier.size(size.dp)) {
        val cx = size.toFloat() / 2
        val cy = size.toFloat() / 2
        val radius = size.toFloat() / 2 - 6

        // Main green arc — ~270 degrees, open at top-right
        drawArc(
            color = MomentaGreen,
            startAngle = -45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Warm dot — positioned at ~1:30 clock position
        val dotAngle = Math.toRadians(45.0)
        val dotDistance = radius * 0.55f
        val dotX = cx + (dotDistance * kotlin.math.cos(dotAngle)).toFloat()
        val dotY = cy - (dotDistance * kotlin.math.sin(dotAngle)).toFloat()

        drawCircle(
            color = MomentaWarm,
            radius = 5.dp.toPx(),
            center = Offset(dotX, dotY)
        )
    }
}

@Composable
fun MomentaLogo(
    modifier: Modifier = Modifier,
    size: Int = 80
) {
    MomentaLogoMark(modifier = modifier, size = size)
}

@Composable
fun MomentaWordmark(
    modifier: Modifier = Modifier,
    showSubtitle: Boolean = true
) {
    Column(modifier = modifier) {
        Text(
            text = "Момента",
            color = MomentaText,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp
        )
        if (showSubtitle) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Один момент. Все вместе.",
                color = MomentaGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            )
        }
    }
}

@Composable
fun MomentaFullLogo(
    modifier: Modifier = Modifier,
    markSize: Int = 72,
    showSubtitle: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MomentaLogoMark(size = markSize)
        Spacer(modifier = Modifier.width(16.dp))
        MomentaWordmark(showSubtitle = showSubtitle)
    }
}
