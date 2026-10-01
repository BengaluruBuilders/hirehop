package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementType
import com.hirehop.core.ui.FactIdTag
import com.hirehop.core.ui.FactSourceProvenance
import com.hirehop.core.ui.RequirementStatusDisc

@Composable
internal fun RequirementRow(
    item: RequirementItem,
    onIHaveThis: () -> Unit,
    onTogglePrepPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            RequirementHeader(item)
            if (item.evidence.isNotEmpty()) EvidenceList(item)
            if (item.isGap) GapActions(item, onIHaveThis, onTogglePrepPlan)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RequirementHeader(item: RequirementItem) {
    val statusLabel = stringResource(item.status.labelRes())
    val priorityLabel = stringResource(item.priorityLabelRes())
    val typeLabel = stringResource(item.requirement.type.typeRes())
    val rowDescription = stringResource(
        R.string.feature_analysis_impl_requirement_row_description,
        item.requirement.text,
        priorityLabel,
        statusLabel,
    )
    val factDescription = item.factRefs.firstOrNull()?.let { ref ->
        stringResource(R.string.feature_analysis_impl_fact_backed, ref.factId)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = listOfNotNull(rowDescription, factDescription).joinToString(" ") },
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        RequirementStatusDisc(status = item.status)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            Text(
                text = item.requirement.text,
                modifier = Modifier.fillMaxWidth(),
                style = HhTheme.typography.titleMedium,
                color = HhTheme.colors.onSurface,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
            ) {
                Text(
                    text = statusLabel,
                    style = HhTheme.typography.labelLarge,
                    color = item.status.statusTint(),
                )
                Text(
                    text = typeLabel,
                    style = HhTheme.typography.monoSmall,
                    color = HhTheme.colors.onSurfaceVariant,
                )
                PriorityTag(item)
            }
            if (item.factRefs.isNotEmpty()) FactChips(item.factRefs)
        }
    }
}

@Composable
private fun PriorityTag(item: RequirementItem) {
    val label = stringResource(item.priorityLabelRes())
    Text(
        text = label.uppercase(),
        modifier = Modifier
            .background(
                color = if (item.isMustHave) HhTheme.colors.onSurface else HhTheme.colors.spotContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(HhTheme.shapes.xs),
            )
            .padding(horizontal = HhTheme.spacing.sm, vertical = HhTheme.spacing.d4 / 2),
        style = HhTheme.typography.labelSmall,
        color = if (item.isMustHave) HhTheme.colors.background else HhTheme.colors.spotInk,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactChips(factRefs: List<RequirementFactRef>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        factRefs.forEach { ref ->
            FactIdTag(factId = ref.factId)
            HhProvenanceChip(
                kind = FactSourceProvenance().confirmedKindOf(ref.source, ref.isConfirmed),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EvidenceList(item: RequirementItem) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        Text(
            text = stringResource(R.string.feature_analysis_impl_evidence_header),
            style = HhTheme.typography.labelSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        item.evidence.forEach { text ->
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GapActions(
    item: RequirementItem,
    onIHaveThis: () -> Unit,
    onTogglePrepPlan: () -> Unit,
) {
    val requirementText = item.requirement.text
    val iHaveThisDescription = stringResource(R.string.feature_analysis_impl_i_have_this_description, requirementText)
    val prepDescription = stringResource(R.string.feature_analysis_impl_add_to_prep_plan_description, requirementText)
    val prepState = stringResource(
        if (item.isInPrepPlan) {
            R.string.feature_analysis_impl_prep_state_added
        } else {
            R.string.feature_analysis_impl_prep_state_not_added
        },
    )
    if (item.isInPrepPlan) {
        Text(
            text = stringResource(R.string.feature_analysis_impl_in_prep_plan),
            style = HhTheme.typography.titleSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            HhOutlinedButton(
                onClick = onIHaveThis,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(min = HhTheme.spacing.d48)
                    .semantics { contentDescription = iHaveThisDescription },
                text = {
                    Text(
                        text = stringResource(R.string.feature_analysis_impl_i_have_this),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
            HhButton(
                onClick = onTogglePrepPlan,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(min = HhTheme.spacing.d48)
                    .semantics {
                        contentDescription = prepDescription
                        stateDescription = prepState
                        role = Role.Switch
                    },
                text = {
                    Text(
                        text = stringResource(R.string.feature_analysis_impl_add_to_prep_plan),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

@StringRes
private fun MatchStatus.labelRes(): Int = when (this) {
    MatchStatus.MET -> R.string.feature_analysis_impl_status_met
    MatchStatus.PARTIAL -> R.string.feature_analysis_impl_status_partial
    MatchStatus.GAP -> R.string.feature_analysis_impl_status_gap
}

@StringRes
private fun RequirementItem.priorityLabelRes(): Int = if (isMustHave) {
    R.string.feature_analysis_impl_priority_must_have
} else {
    R.string.feature_analysis_impl_priority_nice_to_have
}

@StringRes
private fun RequirementType.typeRes(): Int = when (this) {
    RequirementType.SKILL -> R.string.feature_analysis_impl_type_skill
    RequirementType.TOOL -> R.string.feature_analysis_impl_type_tool
    RequirementType.EXPERIENCE -> R.string.feature_analysis_impl_type_experience
    RequirementType.EDUCATION -> R.string.feature_analysis_impl_type_education
    RequirementType.SOFT_SKILL -> R.string.feature_analysis_impl_type_soft_skill
}

private fun MatchStatus.statusKind(): HhStatusKind = when (this) {
    MatchStatus.MET -> HhStatusKind.Met
    MatchStatus.PARTIAL -> HhStatusKind.Partial
    MatchStatus.GAP -> HhStatusKind.Gap
}

@Composable
private fun MatchStatus.statusTint(): Color = when (this) {
    MatchStatus.MET -> HhTheme.colors.success
    MatchStatus.PARTIAL -> HhTheme.colors.warning
    MatchStatus.GAP -> HhTheme.colors.gap
}
