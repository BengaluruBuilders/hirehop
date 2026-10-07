package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhCoverageBar
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhSectionLabel
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.displayKeywords
import com.hirehop.core.model.MatchStatus

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
            top = HhTheme.spacing.sm,
            bottom = contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        if (state.isOffline) {
            item(key = "offline") {
                HhOfflineBanner(message = stringResource(R.string.feature_analysis_impl_offline_banner))
            }
        }
        item(key = "caption") { JobCaption(state) }
        item(key = "coverage") { CoverageCard(state) }
        resultSections(state, actions, onMenuAnchor)
    }
}

@Composable
private fun JobCaption(state: AnalysisUiState.Result) {
    Text(
        text = listOfNotNull(state.headerTitle(), state.headerSubtitle()).joinToString(" · "),
        style = HhTheme.typography.bodyS,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoverageCard(state: AnalysisUiState.Result) {
    val coverage = state.keywordCoverage
    val summary = stringResource(
        R.string.feature_analysis_impl_coverage_title,
        coverage.covered.toString(),
        coverage.total.toString(),
    )
    HhCard(contentPadding = PaddingValues(HhTheme.spacing.lg)) {
        if (coverage.total == 0) {
            Text(
                text = stringResource(R.string.feature_analysis_impl_coverage_empty),
                style = HhTheme.typography.titleM,
                color = HhTheme.colors.onSurface,
            )
            return@HhCard
        }
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = summary },
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = coverage.covered.toString(),
                    style = HhTheme.typography.numeralHero,
                    color = HhTheme.colors.onSurface,
                )
                HhHeadline(
                    text = stringResource(R.string.feature_analysis_impl_coverage_total, coverage.total.toString()),
                    style = HhTheme.typography.headlineM,
                    color = HhTheme.colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(text = summary, style = HhTheme.typography.titleS, color = HhTheme.colors.onSurface)
            HhCoverageBar(
                met = coverage.covered,
                partial = 0,
                gap = coverage.total - coverage.covered,
            )
        }
        SummaryChips(state)
        MissingTermsLine(state.missingKeyTerms())
        if (state.gapCount >= GAP_NOTE_THRESHOLD) GapNote(state.gapCount)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryChips(state: AnalysisUiState.Result) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        listOf(
            Triple(HhStatusKind.Met, state.countOf(MatchStatus.MET), R.string.feature_analysis_impl_summary_met),
            Triple(HhStatusKind.Partial, state.countOf(MatchStatus.PARTIAL), R.string.feature_analysis_impl_summary_partial),
            Triple(HhStatusKind.Gap, state.gapCount, R.string.feature_analysis_impl_summary_gap),
        ).filter { it.second > 0 }.forEach { (kind, count, label) ->
            HhStatusChip(kind = kind, label = stringResource(label, count))
        }
    }
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
            .padding(horizontal = HhTheme.spacing.lg, vertical = 14.dp),
        style = HhTheme.typography.labelL,
        color = HhTheme.colors.onGapContainer,
    )
}

private fun LazyListScope.resultSections(
    state: AnalysisUiState.Result,
    actions: AnalysisActions,
    onMenuAnchor: (String, Rect) -> Unit,
) {
    state.sections.forEach { section ->
        item(key = "header-${section.group}") { GroupHeader(section.group, section.items.size) }
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
private fun GroupHeader(group: RequirementGroup, count: Int) {
    HhSectionLabel(
        text = stringResource(group.titleRes(), count),
        modifier = Modifier.padding(top = HhTheme.spacing.sm),
    )
}

@StringRes
private fun RequirementGroup.titleRes(): Int = when (this) {
    RequirementGroup.MustHaveGaps -> R.string.feature_analysis_impl_group_must_have_gaps
    RequirementGroup.Partial -> R.string.feature_analysis_impl_group_partial
    RequirementGroup.Met -> R.string.feature_analysis_impl_group_met
    RequirementGroup.NiceToHaveGaps -> R.string.feature_analysis_impl_group_nice_to_have_gaps
}
