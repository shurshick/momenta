package com.bghitech.momenta.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bghitech.momenta.R
import com.bghitech.momenta.core.design.MomentaBlue
import com.bghitech.momenta.core.design.MomentaCard
import com.bghitech.momenta.core.design.MomentaFullLogo
import com.bghitech.momenta.core.design.MomentaGreen
import com.bghitech.momenta.core.design.MomentaLargeShape
import com.bghitech.momenta.core.design.MomentaPrimaryButton
import com.bghitech.momenta.core.design.MomentaScreen
import com.bghitech.momenta.core.design.MomentaSurfaceAlt
import com.bghitech.momenta.core.design.MomentaText
import com.bghitech.momenta.core.design.MomentaTextSecondary
import com.bghitech.momenta.core.design.MomentaWarm

@Composable
fun OnboardingScreen(
    onCreateAccount: () -> Unit,
    onLogin: () -> Unit
) {
    MomentaScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            MomentaFullLogo(markSize = 70)

            Spacer(modifier = Modifier.height(42.dp))

            Text(
                text = "\u041e\u0434\u0438\u043d \u043c\u043e\u043c\u0435\u043d\u0442.\n\u0412\u0441\u0435 \u0432\u043c\u0435\u0441\u0442\u0435.",
                color = MomentaGreen,
                fontSize = 34.sp,
                lineHeight = 41.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "\u041a\u0430\u0436\u0434\u044b\u0439 \u0434\u0435\u043d\u044c - \u043d\u043e\u0432\u0430\u044f \u0442\u0435\u043c\u0430.\n\u0421\u043d\u0438\u043c\u0438 \u0444\u043e\u0442\u043e \u0438\u043b\u0438 \u0432\u0438\u0434\u0435\u043e\n\u0438 \u043f\u043e\u0441\u043c\u043e\u0442\u0440\u0438, \u043a\u0430\u043a \u0436\u0438\u0432\u0435\u0442 \u043c\u0438\u0440 \u0441\u0435\u0439\u0447\u0430\u0441.",
                color = MomentaTextSecondary,
                fontSize = 18.sp,
                lineHeight = 26.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.height(34.dp))

            OnboardingLandscapeRow()

            Spacer(modifier = Modifier.height(28.dp))

            MomentaCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(MomentaLargeShape)
                            .background(MomentaGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MomentaGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = "\u041e\u0442\u043a\u0440\u043e\u0439 \u0442\u0435\u043c\u0443 \u0434\u043d\u044f, \u043f\u043e\u0439\u043c\u0430\u0439 \u0441\u0432\u043e\u0439\n\u043c\u043e\u043c\u0435\u043d\u0442 \u0438 \u043f\u043e\u0441\u043c\u043e\u0442\u0440\u0438, \u043a\u0430\u043a \u0436\u0438\u0432\u0435\u0442\n\u043c\u0438\u0440 \u0441\u0435\u0439\u0447\u0430\u0441.",
                        color = MomentaText,
                        fontSize = 17.sp,
                        lineHeight = 25.sp,
                        letterSpacing = 0.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            MomentaPrimaryButton(
                text = "\u0421\u043e\u0437\u0434\u0430\u0442\u044c \u0430\u043a\u043a\u0430\u0443\u043d\u0442",
                onClick = onCreateAccount,
                height = 56.dp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .clip(MomentaLargeShape)
                    .clickable(onClick = onLogin)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "\u0423\u0436\u0435 \u0435\u0441\u0442\u044c \u0430\u043a\u043a\u0430\u0443\u043d\u0442?",
                    color = MomentaTextSecondary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "\u0412\u043e\u0439\u0442\u0438",
                    color = MomentaGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MomentaGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingLandscapeRow() {
    val landscapes = listOf(
        R.drawable.onboarding_landscape_1,
        R.drawable.onboarding_landscape_2,
        R.drawable.onboarding_landscape_3
    )
    val accents = listOf(MomentaGreen, MomentaBlue, MomentaWarm)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        landscapes.forEachIndexed { index, imageRes ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.74f)
                    .clip(MomentaLargeShape)
                    .background(MomentaSurfaceAlt),
                contentAlignment = Alignment.BottomStart
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .width(30.dp)
                        .height(4.dp)
                        .clip(MomentaLargeShape)
                        .background(accents[index])
                )
            }
        }
    }
}
