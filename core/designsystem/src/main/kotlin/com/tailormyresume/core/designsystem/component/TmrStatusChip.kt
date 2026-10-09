package com.tailormyresume.core.designsystem.component

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
import com.tailormyresume.core.designsystem.theme.TmrLightColors
import com.tailormyresume.core.designsystem.theme.TmrTheme

@Composable
fun TmrStatusChip(
    kind: TmrStatusKind,
    modifier: Modifier = Modifier,
    label: String? = null,
    onHero: Boolean = false,
) {
    val text = label ?: kind.defaultLabel()
    if (onHero) {
        TmrHeroStatusChip(kind, text, modifier)
        return
    }
    val color = TmrTheme.colors.statusColor(kind)
    TmrPill(
        container = TmrTheme.colors.primaryContainer,
        content = color,
        height = TmrHeightChip,
        modifier = modifier,
        horizontalPadding = TmrTheme.spacing.md,
    ) {
        TmrStatusMark(kind, color, TmrTheme.colors.primaryContainer, Modifier, TmrSizeChipIcon)
        TmrFitText(text = text, style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold), color = color)
    }
}

@Composable
private fun TmrHeroStatusChip(kind: TmrStatusKind, label: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = TmrTheme.shapes.pill, color = Color.White) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = TmrHeightHeroChip)
                .padding(start = 7.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TmrStatusMark(kind, TmrLightColors.statusColor(kind), Color.White, Modifier, TmrSizeIcon)
            Text(
                text = label,
                style = TmrTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
                color = TmrLightColors.onSurface,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TmrStatusChipPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrStatusChipPreviewColumn() }
}

@Preview(showBackground = true)
@Composable
private fun TmrStatusChipDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrStatusChipPreviewColumn() }
}

@Composable
private fun TmrStatusChipPreviewColumn() {
    Column(
        modifier = Modifier.padding(TmrTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        TmrStatusChip(kind = TmrStatusKind.Met, label = "Met")
        TmrStatusChip(kind = TmrStatusKind.Partial, label = "Partly met")
        TmrStatusChip(kind = TmrStatusKind.Gap, label = "To prepare")
    }
}
