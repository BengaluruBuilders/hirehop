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
    HhPill(
        container = HhTheme.colors.primaryContainer,
        content = color,
        height = HhHeightChip,
        modifier = modifier,
        horizontalPadding = HhTheme.spacing.md,
    ) {
        HhStatusMark(kind, color, HhTheme.colors.primaryContainer, Modifier, HhSizeChipIcon)
        Text(text = text, style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold), color = color)
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
