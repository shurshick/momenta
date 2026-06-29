package com.bghitech.momenta.core.design

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bghitech.momenta.R

@Composable
fun MomentaLogoMark(
    modifier: Modifier = Modifier,
    size: Int = 80
) {
    Image(
        painter = painterResource(id = R.drawable.momenta_logo_official),
        contentDescription = null,
        modifier = modifier.size(size.dp),
        contentScale = ContentScale.Fit
    )
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
            text = "Момент",
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
