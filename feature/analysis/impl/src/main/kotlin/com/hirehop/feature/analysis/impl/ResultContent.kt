package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhDividerStyle
import com.hirehop.core.designsystem.component.HhHeroNumeral
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhSectionCard
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.ui.KeywordCoverageBar
import com.hirehop.core.ui.KeywordCoverageMeter

@Composable
internal fun ResultContent(
    state: AnalysisUiState.Result,
    actions: AnalysisActions,
    modifier: Modifier = Modifier,
) {
    var evidenceRequirementId by rememberSaveable { mutableStateOf<String?>(null) }
    val evidenceTarget = state.sections
        .flatMap { it.items }
        .firstOrNull { it.id == evidenceRequirementId }
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = screenPadding(),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            item(key = "coverage") { CoverageCard(state) }
            resultSections(state.sections, actions.onTogglePrepPlan) { evidenceRequirementId = it }
            item(key = "job-details") { JobDetailsFields(state, actions) }
        }
        SaveBar(state, actions)
    }
    if (evidenceTarget != null) {
        EvidenceDialog(
            requirement = evidenceTarget.requirement,
            onDismiss = { evidenceRequirementId = null },
            onConfirm = { statement ->
                actions.onSubmitEvidence(evidenceTarget.id, statement)
                evidenceRequirementId = null
            },
        )
    }
}

@Composable
private fun CoverageCard(state: AnalysisUiState.Result) {
    val coverage = state.keywordCoverage
    val tally = state.statusTally()
    HhSectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            val allMet = tally.gap == 0 && tally.partial == 0 && state.sections.isNotEmpty()
            if (allMet) {
                BannerLine(
                    text = stringResource(R.string.feature_analysis_impl_all_met_banner),
                    container = HhTheme.colors.successContainer,
                    content = HhTheme.colors.onSuccessContainer,
                )
            } else if (tally.gap > 0) {
                BannerLine(
                    text = pluralStringResource(
                        R.plurals.feature_analysis_impl_gaps_are_tasks,
                        tally.gap,
                        tally.gap,
                    ),
                    container = HhTheme.colors.gapContainer,
                    content = HhTheme.colors.onGapContainer,
                )
            }
            if (coverage.total == 0) {
                Text(
                    text = stringResource(R.string.feature_analysis_impl_coverage_empty),
                    modifier = Modifier.fillMaxWidth(),
                    style = HhTheme.typography.titleMedium,
                    color = HhTheme.colors.onSurface,
                )
            } else {
                HhHeroNumeral(
                    value = stringResource(
                        R.string.feature_analysis_impl_coverage_fraction,
                        coverage.covered,
                        coverage.total,
                    ),
                    caption = stringResource(R.string.feature_analysis_impl_coverage_caption),
                    contentDescription = stringResource(
                        R.string.feature_analysis_impl_coverage_hero_description,
                        coverage.covered,
                        coverage.total,
                    ),
                )
                KeywordCoverageMeter(
                    coverage = coverage,
                    bar = KeywordCoverageBar(segmentCount = coverage.total.coerceAtLeast(1)),
                )
            }
            StatusTally(tally)
        }
    }
}

@Composable
private fun BannerLine(
    text: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(container, RoundedCornerShape(HhTheme.shapes.md))
            .padding(
                horizontal = HhTheme.spacing.md,
                vertical = HhTheme.spacing.sm,
            ),
        style = HhTheme.typography.titleSmall,
        color = content,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusTally(tally: RequirementStatusTally) {
    val description = listOf(
        pluralStringResource(R.plurals.feature_analysis_impl_tally_met, tally.met, tally.met),
        pluralStringResource(R.plurals.feature_analysis_impl_tally_partial, tally.partial, tally.partial),
        pluralStringResource(R.plurals.feature_analysis_impl_tally_gap, tally.gap, tally.gap),
    ).joinToString(", ")
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        HhStatusChip(
            kind = HhStatusKind.Met,
            label = pluralStringResource(R.plurals.feature_analysis_impl_tally_met, tally.met, tally.met),
        )
        HhStatusChip(
            kind = HhStatusKind.Partial,
            label = pluralStringResource(R.plurals.feature_analysis_impl_tally_partial, tally.partial, tally.partial),
        )
        HhStatusChip(
            kind = HhStatusKind.Gap,
            label = pluralStringResource(R.plurals.feature_analysis_impl_tally_gap, tally.gap, tally.gap),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JobDetailsFields(state: AnalysisUiState.Result, actions: AnalysisActions) {
    HhSectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            Text(
                text = stringResource(R.string.feature_analysis_impl_job_details_header),
                modifier = Modifier.semantics { heading() },
                style = HhTheme.typography.labelMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
            HhTextField(
                value = state.title,
                onValueChange = actions.onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.feature_analysis_impl_field_title),
                errorText = if (state.canSave) {
                    null
                } else {
                    stringResource(R.string.feature_analysis_impl_field_title_required)
                },
            )
            HhTextField(
                value = state.company,
                onValueChange = actions.onCompanyChange,
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.feature_analysis_impl_field_company),
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                HhOutlinedButton(
                    onClick = actions.onEditJobText,
                    text = {
                        Text(
                            text = stringResource(R.string.feature_analysis_impl_edit_job_text),
                            style = HhTheme.typography.labelLarge,
                        )
                    },
                )
            }
        }
    }
}

private fun LazyListScope.resultSections(
    sections: List<RequirementSection>,
    onTogglePrepPlan: (String) -> Unit,
    onIHaveThis: (String) -> Unit,
) {
    sections.forEach { section ->
        item(key = "header-${section.group}") { GroupHeader(section) }
        items(section.items, key = { "${section.group}-${it.id}" }) { item ->
            RequirementRow(
                item = item,
                onIHaveThis = { onIHaveThis(item.id) },
                onTogglePrepPlan = { onTogglePrepPlan(item.id) },
            )
            HhDivider()
        }
    }
}

@Composable
private fun GroupHeader(section: RequirementSection) {
    Text(
        text = stringResource(
            R.string.feature_analysis_impl_group_header,
            stringResource(section.group.titleRes()),
            section.items.size,
        ),
        modifier = Modifier
            .padding(top = HhTheme.spacing.sm)
            .semantics { heading() },
        style = HhTheme.typography.monoSmall,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun SaveBar(state: AnalysisUiState.Result, actions: AnalysisActions) {
    HhBottomActionBar(
        modifier = Modifier.fillMaxWidth(),
        creditDisclosure = {
            if (state.prepPlanCount > 0) {
                Text(
                    text = stringResource(
                        R.string.feature_analysis_impl_prep_plan_count,
                        state.prepPlanCount,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    style = HhTheme.typography.labelMedium,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        },
    ) {
        HhButton(
            onClick = actions.onSave,
            enabled = state.canSave,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_analysis_impl_save_and_tailor),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}

internal fun AnalysisUiState.Result.statusTally(): RequirementStatusTally {
    var met = 0
    var partial = 0
    var gap = 0
    sections.forEach { section ->
        section.items.forEach { item ->
            when (item.status) {
                MatchStatus.MET -> met += 1
                MatchStatus.PARTIAL -> partial += 1
                MatchStatus.GAP -> gap += 1
            }
        }
    }
    return RequirementStatusTally(met = met, partial = partial, gap = gap)
}

data class RequirementStatusTally(
    val met: Int,
    val partial: Int,
    val gap: Int,
)

@StringRes
private fun RequirementGroup.titleRes(): Int = when (this) {
    RequirementGroup.MustHaveGaps -> R.string.feature_analysis_impl_group_must_have_gaps
    RequirementGroup.Partial -> R.string.feature_analysis_impl_group_partial
    RequirementGroup.Met -> R.string.feature_analysis_impl_group_met
    RequirementGroup.NiceToHaveGaps -> R.string.feature_analysis_impl_group_nice_to_have_gaps
}
