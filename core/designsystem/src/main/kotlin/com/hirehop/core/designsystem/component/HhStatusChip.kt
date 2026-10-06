package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhLightColors
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhStatusChip(
    kind: HhStatusKind,
    modifier: Modifier = Modifier,
    label: String? = null,
    onHero: Boolean = false,
) {
    val text = label ?: kind.defaultLabel()
    if (onHero) {
        HhHeroStatusChip(kind, text, modifier)
        return
    }
    val color = HhTheme.colors.statusColor(kind)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
    ) {
        HhStatusDisc(kind = kind)
        Text(text = text, style = HhTheme.typography.labelM, color = color)
    }
}

@Composable
private fun HhHeroStatusChip(kind: HhStatusKind, label: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = HhTheme.shapes.pill, color = Color.White) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = HhHeightHeroChip)
                .padding(start = 7.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            HhStatusMark(kind, HhLightColors.statusColor(kind), Color.White, Modifier, HhSizeIcon)
            Text(
                text = label,
                style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
                color = HhLightColors.onSurface,
            )
        }
    }
}

@Composable
fun HhStatusRow(
    kind: HhStatusKind,
    title: String,
    statusLine: String,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    Surface(modifier = modifier, shape = HhTheme.shapes.statusRow, color = colors.card) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = HhHeightStatusRow)
                .padding(horizontal = HhSpacingFourteen, vertical = HhTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            HhStatusDisc(kind = kind, size = HhSizeStepMark)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = HhTheme.typography.titleM, color = colors.onSurface)
                Text(text = statusLine, style = HhTheme.typography.labelM, color = colors.statusColor(kind))
            }
        }
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
        HhStatusRow(HhStatusKind.Met, "SQL for product reporting", "Met · backed by F-02, F-04")
    }
}
