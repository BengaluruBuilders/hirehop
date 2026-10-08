package com.tailormyresume.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.tailormyresume.core.designsystem.component.TmrCoverageBlock
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.KeywordCoverage

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
    val met = coverage.covered.coerceIn(0, coverage.total.coerceAtLeast(0))
    val gap = (coverage.total - met).coerceAtLeast(0)
    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        TmrCoverageBlock(
            met = met,
            partial = null,
            gap = gap,
            caption = stringResource(id = R.string.core_ui_keyword_coverage_caption),
            metLegend = stringResource(id = R.string.core_ui_keyword_coverage_legend_met, met.toString()),
            partialLegend = null,
            gapLegend = stringResource(id = R.string.core_ui_keyword_coverage_legend_gap, gap.toString()),
        )
        Text(
            text = stringResource(id = R.string.core_ui_keyword_coverage_explainer),
            style = TmrTheme.typography.labelM,
            color = TmrTheme.colors.onSurfaceVariant,
        )
    }
}
