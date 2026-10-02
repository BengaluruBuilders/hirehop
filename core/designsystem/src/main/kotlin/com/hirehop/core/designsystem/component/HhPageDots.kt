package com.hirehop.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhPageDots(
    count: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onHeader: Boolean = true,
) {
    val colors = HhTheme.colors
    val active = if (onHeader) colors.onHeader else colors.primary
    val idle = if (onHeader) colors.onHeader.copy(alpha = 0.4f) else colors.outlineSoft
    Row(
        modifier = modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { position ->
            val selected = position == selectedIndex
            Box(
                Modifier
                    .width(if (selected) 20.dp else 8.dp)
                    .height(8.dp)
                    .clip(HhTheme.shapes.pill)
                    .background(if (selected) active else idle),
            )
        }
    }
}

@Preview
@Composable
private fun HhPageDotsPreview() {
    HhPreviewTheme(darkTheme = false) {
        Box(Modifier.background(HhTheme.colors.header).padding(HhTheme.spacing.lg)) {
            HhPageDots(count = 4, selectedIndex = 1)
        }
    }
}

@Preview
@Composable
private fun HhPageDotsDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        Box(Modifier.background(HhTheme.colors.header).padding(HhTheme.spacing.lg)) {
            HhPageDots(count = 4, selectedIndex = 1)
        }
    }
}
