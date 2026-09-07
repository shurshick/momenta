package com.bghitech.momenta.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MomentaEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MomentaLogoMark(size = 54)
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            color = MomentaTextSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(18.dp))
            MomentaPrimaryButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    }
}

@Composable
fun MomentaErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MomentaError,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        MomentaPrimaryButton(
            text = "Повторить",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.7f)
        )
    }
}
