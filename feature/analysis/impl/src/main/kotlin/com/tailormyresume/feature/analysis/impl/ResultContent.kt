package com.tailormyresume.feature.analysis.impl

import android.text.format.DateFormat
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrCoverageBar
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrSectionLabel
import com.tailormyresume.core.designsystem.component.TmrStatusChip
import com.tailormyresume.core.designsystem.component.TmrStatusKind
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.displayKeywords
import com.tailormyresume.core.domain.isKnownSkill
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.time.Instant
import kotlin.time.toJavaInstant

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
            start = TmrTheme.spacing.gutter,
            end = TmrTheme.spacing.gutter,
            top = TmrTheme.spacing.sm,
            bottom = contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        if (state.isOffline) {
            item(key = "offline") {
                TmrOfflineBanner(message = offlineBannerText(state.analysedAt))
            }
            val coverage = state.keywordCoverage
            if (coverage.total > 0) {
                item(key = "coverage-summary") {
                    Text(
                        text = stringResource(
                            R.string.feature_analysis_impl_coverage_title,
                            coverage.covered.toString(),
                            coverage.total.toString(),
                        ),
                        style = TmrTheme.typography.titleS,
                        color = TmrTheme.colors.onSurface,
                    )
                }
            }
            resultSections(state, actions, onMenuAnchor, setOf(RequirementGroup.Met))
            return@LazyColumn
        }
        item(key = "caption") { JobCaption(state) }
        item(key = "coverage") { CoverageCard(state) }
        resultSections(state, actions, onMenuAnchor)
    }
}

@Composable
private fun offlineBannerText(analysedAt: Instant?): String {
    if (analysedAt == null) return stringResource(R.string.feature_analysis_impl_offline_banner)
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val moment = analysedAt.toJavaInstant().atZone(ZoneId.systemDefault())
    val time = DateFormat.getTimeFormat(context).format(Date(analysedAt.toEpochMilliseconds()))
    return if (moment.toLocalDate() == LocalDate.now(moment.zone)) {
        stringResource(R.string.feature_analysis_impl_offline_banner_today, time)
    } else {
        stringResource(
            R.string.feature_analysis_impl_offline_banner_dated,
            moment.format(DateTimeFormatter.ofPattern(OFFLINE_DATE_PATTERN, locale)),
            time,
        )
    }
}

@Composable
private fun JobCaption(state: AnalysisUiState.Result) {
    Text(
        text = listOfNotNull(state.headerTitle(), state.headerSubtitle()).joinToString(" · "),
        style = TmrTheme.typography.bodyS,
        color = TmrTheme.colors.onSurfaceVariant,
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
    TmrCard(contentPadding = PaddingValues(TmrTheme.spacing.lg)) {
        if (coverage.total == 0) {
            Text(
                text = stringResource(R.string.feature_analysis_impl_coverage_empty),
                style = TmrTheme.typography.titleM,
                color = TmrTheme.colors.onSurface,
            )
            return@TmrCard
        }
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = summary },
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = coverage.covered.toString(),
                    style = TmrTheme.typography.numeralHero,
                    color = TmrTheme.colors.onSurface,
                )
                TmrHeadline(
                    text = stringResource(R.string.feature_analysis_impl_coverage_total, coverage.total.toString()),
                    style = TmrTheme.typography.headlineM,
                    color = TmrTheme.colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(text = summary, style = TmrTheme.typography.titleS, color = TmrTheme.colors.onSurface)
            TmrCoverageBar(
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
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        listOf(
            Triple(
                TmrStatusKind.Met,
                state.keywordCoverage.covered,
                R.string.feature_analysis_impl_summary_met,
            ),
            Triple(
                TmrStatusKind.Gap,
                (state.keywordCoverage.total - state.keywordCoverage.covered).coerceAtLeast(0),
                R.string.feature_analysis_impl_summary_gap,
            ),
        ).filter { it.second > 0 }.forEach { (kind, count, label) ->
            TmrStatusChip(kind = kind, label = stringResource(label, count))
        }
    }
}

@Composable
private fun MissingTermsLine(terms: List<String>) {
    if (terms.isEmpty()) return
    Text(
        text = missingTermsText(terms),
        style = TmrTheme.typography.labelM,
        color = TmrTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun missingTermsText(terms: List<String>): AnnotatedString {
    val label = stringResource(R.string.feature_analysis_impl_missing_terms_label)
    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TmrTheme.colors.gap)) {
            append(label)
        }
        append(' ')
        append(terms.joinToString())
    }
}

private const val OFFLINE_DATE_PATTERN = "d MMM"

private val companyWordSeparator = Regex("[^\\p{L}\\p{N}]+")

private fun AnalysisUiState.Result.missingKeyTerms(): List<String> {
    val companyWords = job.company
        .split(companyWordSeparator)
        .filter { it.isNotBlank() }
        .map { it.lowercase() }
        .toSet()
    return items.filter { it.isGap }
        .flatMap { displayKeywords(it.requirement) }
        .filter { it.isNotBlank() }
        .distinct()
        .filterNot { it.lowercase() in companyWords && !isKnownSkill(it) }
}

@Composable
private fun GapNote(gapCount: Int) {
    Text(
        text = pluralStringResource(R.plurals.feature_analysis_impl_gap_note, gapCount, gapCount),
        modifier = Modifier
            .fillMaxWidth()
            .background(TmrTheme.colors.gapContainer, TmrTheme.shapes.banner)
            .padding(horizontal = TmrTheme.spacing.lg, vertical = 14.dp),
        style = TmrTheme.typography.labelL,
        color = TmrTheme.colors.onGapContainer,
    )
}

private fun LazyListScope.resultSections(
    state: AnalysisUiState.Result,
    actions: AnalysisActions,
    onMenuAnchor: (String, Rect) -> Unit,
    groups: Set<RequirementGroup> = RequirementGroup.entries.toSet(),
) {
    state.sections.filter { it.group in groups }.forEach { section ->
        item(key = "header-${section.group}") { GroupHeader(section.group, section.items.size) }
        items(section.items.size, key = { "requirement-${section.items[it].id}" }) { index ->
            val item = section.items[index]
            RequirementRow(
                item = item,
                isClosed = item.id == state.closedRequirementId,
                actions = actions,
                onMenuAnchor = { bounds -> onMenuAnchor(item.id, bounds) },
                modifier = Modifier.animateItem(placementSpec = TmrTheme.motion.proofSpecs.offset),
            )
        }
    }
}

@Composable
private fun GroupHeader(group: RequirementGroup, count: Int) {
    TmrSectionLabel(
        text = stringResource(group.titleRes(), count),
        modifier = Modifier.padding(top = TmrTheme.spacing.sm),
    )
}

@StringRes
private fun RequirementGroup.titleRes(): Int = when (this) {
    RequirementGroup.MustHaveGaps -> R.string.feature_analysis_impl_group_must_have_gaps
    RequirementGroup.Partial -> R.string.feature_analysis_impl_group_partial
    RequirementGroup.Met -> R.string.feature_analysis_impl_group_met
    RequirementGroup.NiceToHaveGaps -> R.string.feature_analysis_impl_group_nice_to_have_gaps
}
