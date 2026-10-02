package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.hirehop.core.designsystem.component.HhCoverageBlock
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
internal fun ResultContent(
    state: AnalysisUiState.Result,
    actions: AnalysisActions,
    contentPadding: PaddingValues,
    onMenuAnchor: (String, Rect) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = HhTheme.spacing.gutter,
            end = HhTheme.spacing.gutter,
            bottom = contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.xxs),
    ) {
        if (state.isOffline) {
            item(key = "offline") {
                HhOfflineBanner(message = stringResource(R.string.feature_analysis_impl_offline_banner))
            }
        }
        item(key = "coverage") { CoverageCard(state) }
        resultSections(state, actions, onMenuAnchor)
    }
}

@Composable
private fun CoverageCard(state: AnalysisUiState.Result) {
    val coverage = state.keywordCoverage
    val toPrepare = coverage.total - coverage.covered
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.lg)) {
        if (coverage.total == 0) {
            Text(
                text = stringResource(R.string.feature_analysis_impl_coverage_empty),
                style = HhTheme.typography.titleM,
                color = HhTheme.colors.onSurface,
            )
            return@HhHeroCard
        }
        val description = stringResource(
            R.string.feature_analysis_impl_coverage_title,
            coverage.covered.toString(),
            coverage.total.toString(),
        )
        HhCoverageBlock(
            met = coverage.covered,
            partial = null,
            gap = toPrepare,
            caption = stringResource(R.string.feature_analysis_impl_coverage_caption),
            metLegend = stringResource(R.string.feature_analysis_impl_coverage_met, coverage.covered.toString()),
            partialLegend = null,
            gapLegend = stringResource(R.string.feature_analysis_impl_coverage_gap, toPrepare.toString()),
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
            summary = description,
        )
        Text(
            text = stringResource(R.string.feature_analysis_impl_coverage_note),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
        if (state.gapCount >= GAP_NOTE_THRESHOLD) GapNote(state.gapCount)
    }
}

@Composable
private fun GapNote(gapCount: Int) {
    Text(
        text = pluralStringResource(R.plurals.feature_analysis_impl_gap_note, gapCount, gapCount),
        modifier = Modifier
            .fillMaxWidth()
            .background(HhTheme.colors.metContainer, HhTheme.shapes.banner)
            .padding(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.md),
        style = HhTheme.typography.bodyM,
        color = HhTheme.colors.onMetContainer,
    )
}

private fun LazyListScope.resultSections(
    state: AnalysisUiState.Result,
    actions: AnalysisActions,
    onMenuAnchor: (String, Rect) -> Unit,
) {
    state.sections.forEach { section ->
        item(key = "header-${section.group}") { GroupHeader(section.group) }
        items(section.items.size, key = { "requirement-${section.items[it].id}" }) { index ->
            val item = section.items[index]
            RequirementRow(
                item = item,
                isClosed = item.id == state.closedRequirementId,
                actions = actions,
                onMenuAnchor = { bounds -> onMenuAnchor(item.id, bounds) },
                modifier = Modifier.animateItem(placementSpec = HhTheme.motion.proofSpecs.offset),
            )
        }
    }
}

@Composable
private fun GroupHeader(group: RequirementGroup) {
    Text(
        text = stringResource(group.titleRes()),
        modifier = Modifier
            .padding(top = HhTheme.spacing.xs)
            .semantics { heading() },
        style = HhTheme.typography.titleS,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@StringRes
private fun RequirementGroup.titleRes(): Int = when (this) {
    RequirementGroup.MustHaveGaps -> R.string.feature_analysis_impl_group_must_have_gaps
    RequirementGroup.Partial -> R.string.feature_analysis_impl_group_partial
    RequirementGroup.Met -> R.string.feature_analysis_impl_group_met
    RequirementGroup.NiceToHaveGaps -> R.string.feature_analysis_impl_group_nice_to_have_gaps
}
