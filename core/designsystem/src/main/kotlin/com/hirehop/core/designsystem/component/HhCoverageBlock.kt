package com.hirehop.core.designsystem.component

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
import com.hirehop.core.designsystem.theme.HhTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HhCoverageBlock(
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
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            itemVerticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "$met / ${met + partialCount + gap}",
                style = HhTheme.typography.numeralHero,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = caption,
                modifier = Modifier.padding(bottom = HhTheme.spacing.xs),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        if (summary != null) {
            Text(text = summary, style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurface)
        }
        HhCoverageBar(met = met, partial = partialCount, gap = gap)
        HhCoverageLegend(metLegend, partialLegend.takeIf { partial != null }, gapLegend)
    }
}

@Composable
fun HhCoverageBar(
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
        repeat(met) { HhSegment(HhStatusKind.Met, Modifier.weight(1f)) }
        repeat(partial) { HhSegment(HhStatusKind.Partial, Modifier.weight(1f)) }
        repeat(gap) { HhSegment(HhStatusKind.Gap, Modifier.weight(1f)) }
    }
}

@Composable
private fun HhSegment(kind: HhStatusKind, modifier: Modifier) {
    val colors = HhTheme.colors
    val tint = colors.statusColor(kind)
    val shape = RoundedCornerShape(3.dp)
    val base = modifier.height(HhSizeSegment).clip(shape)
    when (kind) {
        HhStatusKind.Met -> Box(base.background(tint))
        HhStatusKind.Partial -> Box(base.border(HhWidthStroke, tint, shape)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(0.5f).background(tint))
        }
        HhStatusKind.Gap -> Box(base.hhDashedBorder(tint, HhWidthStroke, 3.dp))
    }
}

@Composable
private fun HhCoverageLegend(metLegend: String, partialLegend: String?, gapLegend: String) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        HhStatusChip(kind = HhStatusKind.Met, label = metLegend)
        if (partialLegend != null) {
            HhStatusChip(kind = HhStatusKind.Partial, label = partialLegend)
        }
        HhStatusChip(kind = HhStatusKind.Gap, label = gapLegend)
    }
}

@Preview(showBackground = true)
@Composable
private fun HhCoverageBlockPreview() {
    HhPreviewTheme(darkTheme = false) { HhCoverageBlockSample() }
}

@Preview(showBackground = true)
@Composable
private fun HhCoverageBlockDarkPreview() {
    HhPreviewTheme(darkTheme = true) { HhCoverageBlockSample() }
}

@Composable
private fun HhCoverageBlockSample() {
    HhHeroCard(modifier = Modifier.padding(HhTheme.spacing.gutter)) {
        HhCoverageBlock(
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
