package com.hirehop.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhSegmentedCounter(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        Text(
            text = "$current / $total",
            style = HhTheme.typography.numeralM,
            color = colors.onSurface,
        )
        if (total in 1..MAX_SEGMENTS) {
            Row(
                modifier = Modifier.fillMaxWidth().clearAndSetSemantics {},
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(total) { position ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(HhSizeSegment / 2)
                            .clip(HhTheme.shapes.pill)
                            .background(if (position < current) colors.primary else colors.outlineVariant),
                    )
                }
            }
        }
    }
}

private const val MAX_SEGMENTS = 24

@Preview(showBackground = true)
@Composable
private fun HhSegmentedCounterPreview() {
    HhPreviewTheme(darkTheme = false) { HhSegmentedCounterSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhSegmentedCounterDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhSegmentedCounterSample() }
}

@Composable
private fun HhSegmentedCounterSample() {
    HhSegmentedCounter(current = 3, total = 5, modifier = Modifier.padding(HhTheme.spacing.lg))
}
