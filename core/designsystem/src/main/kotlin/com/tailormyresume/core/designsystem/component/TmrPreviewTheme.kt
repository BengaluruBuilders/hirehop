package com.tailormyresume.core.designsystem.component

import androidx.compose.runtime.Composable
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
internal fun TmrPreviewTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    TmrTheme(darkTheme = darkTheme, content = content)
}
