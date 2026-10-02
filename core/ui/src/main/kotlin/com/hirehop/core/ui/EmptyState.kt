package com.hirehop.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.hirehop.core.designsystem.theme.HhTheme

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
            .padding(HhTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        illustration?.invoke()
        Text(
            text = title,
            style = HhTheme.typography.titleL,
            color = HhTheme.colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        action?.invoke()
    }
}
