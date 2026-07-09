package com.bghitech.momenta.core.design

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MomentaScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    centered: Boolean = false,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MomentaSpacing.screenHorizontal, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
        ) {
            Text(
                text = title,
                color = if (centered) MomentaGreen else MomentaText,
                fontSize = 25.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                letterSpacing = 0.sp
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = if (centered) MomentaWarm else MomentaTextSecondary,
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    fontWeight = if (centered) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                    letterSpacing = 0.sp
                )
            }
        }
        trailing?.invoke()
    }
}
