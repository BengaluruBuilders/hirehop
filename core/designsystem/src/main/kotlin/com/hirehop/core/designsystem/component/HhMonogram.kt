package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

private val HhMonogramShape = RoundedCornerShape(14.dp)

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
            .clip(HhMonogramShape)
            .drawBehind { drawRect(colors.brand) }
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        val letter = text.trim().take(1).uppercase()
        if (letter.isEmpty()) {
            Icon(HhIcons.Applications, contentDescription = null, tint = colors.onBrand, modifier = Modifier.size(size / 2))
        } else {
            Text(text = letter, style = HhTheme.typography.titleL, color = colors.onBrand)
        }
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
