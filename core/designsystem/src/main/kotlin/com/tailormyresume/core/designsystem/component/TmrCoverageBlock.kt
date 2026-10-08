package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.theme.TmrTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TmrCoverageBlock(
    met: Int,
    partial: Int?,
    gap: Int,
    caption: String,
    metLegend: String,
    partialLegend: String?,
    gapLegend: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
) {
    val partialCount = partial ?: 0
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            itemVerticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "$met / ${met + partialCount + gap}",
                style = TmrTheme.typography.numeralHero,
                color = TmrTheme.colors.onSurface,
            )
            Text(
                text = caption,
                modifier = Modifier.padding(bottom = TmrTheme.spacing.xs),
                style = TmrTheme.typography.bodyM,
                color = TmrTheme.colors.onSurfaceVariant,
            )
        }
        if (summary != null) {
            Text(text = summary, style = TmrTheme.typography.bodyM, color = TmrTheme.colors.onSurface)
        }
        TmrCoverageBar(met = met, partial = partialCount, gap = gap)
        TmrCoverageLegend(metLegend, partialLegend.takeIf { partial != null }, gapLegend)
    }
}

@Composable
fun TmrCoverageBar(
    met: Int,
    partial: Int,
    gap: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(met) { TmrSegment(TmrStatusKind.Met, Modifier.weight(1f)) }
        repeat(partial) { TmrSegment(TmrStatusKind.Partial, Modifier.weight(1f)) }
        repeat(gap) { TmrSegment(TmrStatusKind.Gap, Modifier.weight(1f)) }
    }
}

@Composable
private fun TmrSegment(kind: TmrStatusKind, modifier: Modifier) {
    val colors = TmrTheme.colors
    val tint = colors.statusColor(kind)
    val shape = RoundedCornerShape(3.dp)
    val base = modifier.height(TmrSizeSegment).clip(shape)
    when (kind) {
        TmrStatusKind.Met -> Box(base.background(tint))
        TmrStatusKind.Partial -> Box(base.border(TmrWidthStroke, tint, shape)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(0.5f).background(tint))
        }
        TmrStatusKind.Gap -> Box(base.tmrDashedBorder(tint, TmrWidthStroke, 3.dp))
    }
}

@Composable
private fun TmrCoverageLegend(metLegend: String, partialLegend: String?, gapLegend: String) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
    ) {
        TmrStatusChip(kind = TmrStatusKind.Met, label = metLegend)
        if (partialLegend != null) {
            TmrStatusChip(kind = TmrStatusKind.Partial, label = partialLegend)
        }
        TmrStatusChip(kind = TmrStatusKind.Gap, label = gapLegend)
    }
}

@Preview(showBackground = true)
@Composable
private fun TmrCoverageBlockPreview() {
    TmrPreviewTheme(darkTheme = false) { TmrCoverageBlockSample() }
}

@Preview(showBackground = true)
@Composable
private fun TmrCoverageBlockDarkPreview() {
    TmrPreviewTheme(darkTheme = true) { TmrCoverageBlockSample() }
}

@Composable
private fun TmrCoverageBlockSample() {
    TmrHeroCard(modifier = Modifier.padding(TmrTheme.spacing.gutter)) {
        TmrCoverageBlock(
            met = 9,
            partial = 2,
            gap = 3,
            caption = "key terms covered",
            metLegend = "9 Met",
            partialLegend = "2 Partly met",
            gapLegend = "3 To prepare",
        )
    }
}
