package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhSegmentedCounter(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        Text(
            text = current.toString(),
            style = HhTheme.typography.monoSmall.copy(fontFeatureSettings = HH_TABULAR_FIGURES),
            color = colors.primary,
        )
        Text(
            text = hhOfLabel(total),
            style = HhTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Start,
        )
    }
}

private fun hhOfLabel(total: Int): String = " of $total"

@Preview(showBackground = true)
@Composable
private fun HhSegmentedCounterPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhSegmentedCounter(current = 3, total = 7, modifier = Modifier.padding(HhTheme.spacing.lg))
    }
}

@Preview(showBackground = true)
@Composable
private fun HhSegmentedCounterDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhSegmentedCounter(current = 3, total = 7, modifier = Modifier.padding(HhTheme.spacing.lg))
    }
}
