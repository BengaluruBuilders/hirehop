package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhCoverageBar
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.displayKeywords

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
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md + HhTheme.spacing.xxs),
    ) {
        if (state.isOffline) {
            item(key = "offline") {
                HhOfflineBanner(message = stringResource(R.string.feature_analysis_impl_offline_banner))
            }
        }
        item(key = "coverage") { CoverageCard(state) }
        mustHavesHeading(state)
        resultSections(state, actions, onMenuAnchor)
    }
}

@Composable
private fun CoverageCard(state: AnalysisUiState.Result) {
    val coverage = state.keywordCoverage
    val summary = stringResource(
        R.string.feature_analysis_impl_coverage_title,
        coverage.covered.toString(),
        coverage.total.toString(),
    )
    HhCard(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = summary },
        contentPadding = PaddingValues(HhTheme.spacing.cardPadding),
    ) {
        CoverageLabel()
        if (coverage.total == 0) {
            Text(
                text = stringResource(R.string.feature_analysis_impl_coverage_empty),
                style = HhTheme.typography.titleM,
                color = HhTheme.colors.onSurface,
            )
            return@HhCard
        }
        Text(text = summary, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
        HhCoverageBar(
            met = coverage.covered,
            partial = 0,
            gap = coverage.total - coverage.covered,
        )
        MissingTermsLine(state.missingKeyTerms())
        if (state.gapCount >= GAP_NOTE_THRESHOLD) GapNote(state.gapCount)
    }
}

@Composable
private fun CoverageLabel() {
    Text(
        text = stringResource(R.string.feature_analysis_impl_keyword_coverage),
        style = HhTheme.typography.labelM,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun MissingTermsLine(terms: List<String>) {
    if (terms.isEmpty()) return
    Text(
        text = missingTermsText(terms),
        style = HhTheme.typography.labelM,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun missingTermsText(terms: List<String>): AnnotatedString {
    val label = stringResource(R.string.feature_analysis_impl_missing_terms_label)
    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = HhTheme.colors.gap)) {
            append(label)
        }
        append(' ')
        append(terms.joinToString())
    }
}

private fun AnalysisUiState.Result.missingKeyTerms(): List<String> =
    items.filter { it.isGap }
        .flatMap { displayKeywords(it.requirement) }
        .filter { it.isNotBlank() }
        .distinct()

@Composable
private fun GapNote(gapCount: Int) {
    Text(
        text = pluralStringResource(R.plurals.feature_analysis_impl_gap_note, gapCount, gapCount),
        modifier = Modifier
            .fillMaxWidth()
            .background(HhTheme.colors.gapContainer, HhTheme.shapes.banner)
            .padding(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.md),
        style = HhTheme.typography.bodyM,
        color = HhTheme.colors.onGapContainer,
    )
}

private fun LazyListScope.mustHavesHeading(state: AnalysisUiState.Result) {
    val mustHaveCount = state.items.count { it.isMustHave }
    if (mustHaveCount == 0) return
    item(key = "must-haves") { MustHavesHeading(mustHaveCount, state.items.size) }
}

@Composable
private fun MustHavesHeading(mustHaveCount: Int, totalCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = HhTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.feature_analysis_impl_must_haves_first),
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
            style = HhTheme.typography.titleL,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(
                R.string.feature_analysis_impl_must_haves_count,
                mustHaveCount,
                totalCount,
            ),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
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
            .padding(top = HhTheme.spacing.sm)
            .semantics { heading() },
        style = HhTheme.typography.titleM,
        color = HhTheme.colors.onSurface,
    )
}

@StringRes
private fun RequirementGroup.titleRes(): Int = when (this) {
    RequirementGroup.MustHaveGaps -> R.string.feature_analysis_impl_group_must_have_gaps
    RequirementGroup.Partial -> R.string.feature_analysis_impl_group_partial
    RequirementGroup.Met -> R.string.feature_analysis_impl_group_met
    RequirementGroup.NiceToHaveGaps -> R.string.feature_analysis_impl_group_nice_to_have_gaps
}
