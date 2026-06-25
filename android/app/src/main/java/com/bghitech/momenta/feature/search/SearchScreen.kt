package com.bghitech.momenta.feature.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bghitech.momenta.core.design.*

@Composable
fun SearchScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Поиск",
            color = MomentaText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Найти моменты, людей...", color = MomentaTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MomentaText,
                unfocusedTextColor = MomentaText,
                cursorColor = MomentaGreen,
                focusedBorderColor = MomentaGreen,
                unfocusedBorderColor = MomentaDivider
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Скоро здесь будет поиск",
                color = MomentaTextSecondary,
                fontSize = 14.sp
            )
        }
    }
}
