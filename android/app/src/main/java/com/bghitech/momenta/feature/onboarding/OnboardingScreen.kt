package com.bghitech.momenta.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bghitech.momenta.core.design.*
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showContent = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            MomentaLogo(size = 120)

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Добро пожаловать\nв Момента",
                color = MomentaText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Запечатлейте то, что делает каждый день уникальным.\nОдин момент. Один день. Весь мир.",
                color = MomentaTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Камера • Момент • Время",
                color = MomentaWarm,
                fontSize = 13.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            MomentaPrimaryButton(
                text = "Начать",
                onClick = onGetStarted
            )
        }
    }
}
