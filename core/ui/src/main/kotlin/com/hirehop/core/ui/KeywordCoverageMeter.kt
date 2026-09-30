package com.hirehop.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.hirehop.core.model.KeywordCoverage

@Composable
fun KeywordCoverageMeter(
    coverage: KeywordCoverage,
    modifier: Modifier = Modifier,
    bar: KeywordCoverageBar = KeywordCoverageBar(),
) {
    val description = stringResource(
        id = R.string.core_ui_keyword_coverage_content_description,
        coverage.covered,
        coverage.total,
    )
    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = pluralStringResource(
                id = R.plurals.core_ui_keyword_coverage_fraction,
                coverage.total,
                coverage.covered,
                coverage.total,
            ),
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            bar.segmentFills(coverage).forEach { fill ->
                CoverageSegment(
                    fill = fill,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                )
            }
        }
        Text(
            text = stringResource(id = R.string.core_ui_keyword_coverage_explainer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CoverageSegment(
    fill: SegmentFill,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(2.dp)
    val filledColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = modifier
            .background(color = if (fill == SegmentFill.FILLED) filledColor else emptyColor, shape = shape)
            .border(width = 1.dp, color = borderColor, shape = shape),
    )
}
