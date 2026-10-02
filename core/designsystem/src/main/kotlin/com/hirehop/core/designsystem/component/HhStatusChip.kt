package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhStatusChip(
    kind: HhStatusKind,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val color = HhTheme.colors.statusColor(kind)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
    ) {
        HhStatusDisc(kind = kind)
        Text(
            text = label ?: kind.defaultLabel(),
            style = HhTheme.typography.labelM,
            color = color,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HhStatusChipPreview() {
    HhPreviewTheme(darkTheme = false) { HhStatusChipPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun HhStatusChipDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhStatusChipPreviewColumn() }
}

@Composable
private fun HhStatusChipPreviewColumn() {
    Column(
        modifier = Modifier.padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhStatusChip(kind = HhStatusKind.Met, label = "Met")
        HhStatusChip(kind = HhStatusKind.Partial, label = "Partly met")
        HhStatusChip(kind = HhStatusKind.Gap, label = "To prepare")
    }
}
