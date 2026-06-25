package com.bghitech.momenta.core.permissions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bghitech.momenta.core.design.MomentaPrimaryButton
import com.bghitech.momenta.core.design.MomentaTextSecondary
import com.bghitech.momenta.core.design.MomentaText

@Composable
fun CameraPermissionContent(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Для съёмки моментов нужен доступ к камере",
                color = MomentaText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Момента использует камеру, чтобы вы могли делиться своим днём с миром.",
                color = MomentaTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            MomentaPrimaryButton(
                text = "Разрешить",
                onClick = onRequestPermission
            )
        }
    }
}
