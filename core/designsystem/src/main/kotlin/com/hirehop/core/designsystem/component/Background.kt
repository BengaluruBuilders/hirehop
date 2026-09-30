package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.LocalBackgroundTheme

@Composable
fun HhBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val backgroundTheme = LocalBackgroundTheme.current
    Surface(
        color = if (backgroundTheme.color == Color.Unspecified) {
            Color.Transparent
        } else {
            backgroundTheme.color
        },
        tonalElevation = if (backgroundTheme.tonalElevation == Dp.Unspecified) {
            0.dp
        } else {
            backgroundTheme.tonalElevation
        },
        modifier = modifier.fillMaxSize(),
    ) {
        CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
            content()
        }
    }
}
