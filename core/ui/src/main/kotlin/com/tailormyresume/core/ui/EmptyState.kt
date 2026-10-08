package com.tailormyresume.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    illustration: (@Composable () -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(TmrTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        illustration?.invoke()
        Text(
            text = title,
            style = TmrTheme.typography.titleL,
            color = TmrTheme.colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = TmrTheme.typography.bodyM,
            color = TmrTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        action?.invoke()
    }
}
