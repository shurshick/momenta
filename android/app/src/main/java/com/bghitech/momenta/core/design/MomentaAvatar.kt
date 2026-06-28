package com.bghitech.momenta.core.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.rememberAsyncImagePainter
import com.bghitech.momenta.R

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

    val avatarRes = avatarResourceFor(avatarKey)
    if (avatarRes != null) {
        Image(
            painter = painterResource(avatarRes),
            contentDescription = username,
            modifier = modifier.size(size).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        return
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(MomentaGreen.copy(alpha = 0.9f), MomentaSurfaceAlt))),
        contentAlignment = Alignment.Center
    ) {
    }
}

private fun avatarResourceFor(avatarKey: String?): Int? = when (avatarKey) {
    "avatar_01" -> R.drawable.avatar_01
    "avatar_02" -> R.drawable.avatar_02
    "avatar_03" -> R.drawable.avatar_03
    "avatar_04" -> R.drawable.avatar_04
    "avatar_05" -> R.drawable.avatar_05
    "avatar_06" -> R.drawable.avatar_06
    "avatar_07" -> R.drawable.avatar_07
    "avatar_08" -> R.drawable.avatar_08
    "avatar_09" -> R.drawable.avatar_09
    "avatar_10" -> R.drawable.avatar_10
    "avatar_11" -> R.drawable.avatar_11
    "avatar_12" -> R.drawable.avatar_12
    "avatar_13" -> R.drawable.avatar_13
    "avatar_14" -> R.drawable.avatar_14
    "avatar_15" -> R.drawable.avatar_15
    "avatar_16" -> R.drawable.avatar_16
    "avatar_17" -> R.drawable.avatar_17
    "avatar_18" -> R.drawable.avatar_18
    "avatar_19" -> R.drawable.avatar_19
    "avatar_20" -> R.drawable.avatar_20
    "avatar_21" -> R.drawable.avatar_21
    "avatar_22" -> R.drawable.avatar_22
    "avatar_23" -> R.drawable.avatar_23
    "avatar_24" -> R.drawable.avatar_24
    "avatar_25" -> R.drawable.avatar_25
    "avatar_26" -> R.drawable.avatar_26
    "avatar_27" -> R.drawable.avatar_27
    "avatar_28" -> R.drawable.avatar_28
    "avatar_29" -> R.drawable.avatar_29
    "avatar_30" -> R.drawable.avatar_30
    "avatar_31" -> R.drawable.avatar_31
    "avatar_32" -> R.drawable.avatar_32
    "avatar_33" -> R.drawable.avatar_33
    "avatar_34" -> R.drawable.avatar_34
    "avatar_35" -> R.drawable.avatar_35
    "avatar_36" -> R.drawable.avatar_36
    "avatar_37" -> R.drawable.avatar_37
    "avatar_38" -> R.drawable.avatar_38
    "avatar_39" -> R.drawable.avatar_39
    "avatar_40" -> R.drawable.avatar_40
    else -> null
}
