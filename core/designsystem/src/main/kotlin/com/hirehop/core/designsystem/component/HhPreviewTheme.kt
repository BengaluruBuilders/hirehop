package com.hirehop.core.designsystem.component

import androidx.compose.runtime.Composable
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
internal fun HhPreviewTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    HhTheme(darkTheme = darkTheme, content = content)
}
