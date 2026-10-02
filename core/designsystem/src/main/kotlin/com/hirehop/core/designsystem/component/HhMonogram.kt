package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhMonogram(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = HhSizeMonogram,
) {
    val colors = HhTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(HhTheme.shapes.pill)
            .drawBehind { drawRect(colors.primaryContainer) }
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.trim().take(1).uppercase(),
            style = HhTheme.typography.titleM,
            color = colors.onPrimaryContainer,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HhMonogramPreview() {
    HhPreviewTheme(darkTheme = false) { HhMonogram(text = "Kestrel Labs") }
}

@Preview(showBackground = true)
@Composable
private fun HhMonogramDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhMonogram(text = "Kestrel Labs") }
}
