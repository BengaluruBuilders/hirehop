package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun TmrMonogram(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = TmrSizeMonogram,
    shape: Shape = TmrTheme.shapes.monogram,
) {
    val colors = TmrTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .drawBehind { drawRect(colors.brand) }
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        val letter = text.trim().take(1).uppercase()
        if (letter.isEmpty()) {
            Icon(TmrIcons.Applications, contentDescription = null, tint = colors.onBrand, modifier = Modifier.size(size / 2))
        } else {
            Text(text = letter, style = TmrTheme.typography.titleL, color = colors.onBrand)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TmrMonogramPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrMonogram(text = "Kestrel Labs") }
}

@Preview(showBackground = true)
@Composable
private fun TmrMonogramDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrMonogram(text = "Kestrel Labs") }
}
