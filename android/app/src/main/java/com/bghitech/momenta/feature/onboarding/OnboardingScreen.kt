package com.bghitech.momenta.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
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
    onGetStarted: () -> Unit
) {
    MomentaScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            MomentaFullLogo(markSize = 82)
            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = stringResource(R.string.slogan),
                color = MomentaGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.onboarding_desc),
                color = MomentaTextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 23.sp
            )

            Spacer(modifier = Modifier.height(34.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(MomentaGreen, MomentaWarm, MomentaBlue).forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.72f)
                            .clip(MomentaLargeShape)
                            .background(MomentaSurfaceAlt),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(14.dp)
                                .width((28 + index * 8).dp)
                                .height(4.dp)
                                .clip(MomentaLargeShape)
                                .background(color)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            MomentaCard {
                Text(
                    text = "Открой тему дня, поймай свой момент и посмотри, как живёт мир сейчас.",
                    color = MomentaText,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            MomentaPrimaryButton(
                text = stringResource(R.string.onboarding_get_started),
                onClick = onGetStarted
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
