package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.designsystem.component.HhRequirementTag
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.component.HhTermChip
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.MatchStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RequirementRow(
    item: RequirementItem,
    isClosed: Boolean,
    actions: AnalysisActions,
    onMenuAnchor: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hop = remember { Animatable(1f) }
    val hopSpec = HhTheme.motion.hopSpecs.scale
    LaunchedEffect(isClosed) {
        if (isClosed) {
            hop.snapTo(HOP_FROM_SCALE)
            hop.animateTo(1f, hopSpec)
        }
    }
    val statusLabel = stringResource(item.status.labelRes())
    val priorityLabel = stringResource(item.priorityLabelRes())
    val description = rowDescription(item, statusLabel, priorityLabel)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = hop.value
                scaleY = hop.value
            }
            .semantics { contentDescription = description },
    ) {
        HhCard {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = HhTheme.spacing.touch - HhTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                HhStatusChip(kind = item.status.statusKind(), label = statusLabel)
                HhRequirementTag(label = priorityLabel, mustHave = item.isMustHave)
            }
            Text(
                text = item.requirement.text,
                style = HhTheme.typography.titleM,
                color = HhTheme.colors.onSurface,
            )
            RowFooter(item, actions)
        }
        if (item.hasMenu) {
            HhIconButton(
                icon = HhIcons.More,
                contentDescription = stringResource(R.string.feature_analysis_impl_more_actions),
                onClick = { actions.onOpenMenu(item.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = HhTheme.spacing.xxs, end = HhTheme.spacing.xs)
                    .onGloballyPositioned { onMenuAnchor(it.boundsInRoot()) },
                tint = HhTheme.colors.onSurfaceVariant,
                containerColor = Color.Transparent,
                borderColor = Color.Transparent,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RowFooter(item: RequirementItem, actions: AnalysisActions) {
    if (item.isGap && !item.hasSource) {
        GapActions(item, actions)
        return
    }
    if (!item.hasSource) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        item.factRefs.forEach { ref -> SourceFactChip(ref.displayId) { actions.onSeeSource(item.id) } }
        item.skills.forEach { skill -> HhTermChip(label = skill) }
        if (item.hasUserStatedFact) {
            HhProvenanceChip(
                kind = HhProvenanceKind.UserStated,
                label = stringResource(R.string.feature_analysis_impl_provenance_user_stated),
            )
        }
    }
}

@Composable
private fun GapActions(item: RequirementItem, actions: AnalysisActions) {
    val stacked = LocalDensity.current.fontScale >= STACK_FONT_SCALE
    val prepDescription = stringResource(R.string.feature_analysis_impl_prep_description, item.requirement.text)
    val prepState = stringResource(
        if (item.isInPrepPlan) R.string.feature_analysis_impl_prep_added else R.string.feature_analysis_impl_prep_not_added,
    )
    val iHaveThis: @Composable (Modifier) -> Unit = { modifier ->
        HhOutlineButton(
            label = stringResource(R.string.feature_analysis_impl_i_have_this),
            onClick = { actions.onIHaveThis(item.id) },
            modifier = modifier,
        )
    }
    val prepPlan: @Composable (Modifier) -> Unit = { modifier ->
        val semantic = modifier.semantics {
            contentDescription = prepDescription
            stateDescription = prepState
            role = Role.Switch
        }
        if (item.isInPrepPlan) {
            HhSecondaryButton(
                label = stringResource(R.string.feature_analysis_impl_add_to_prep_plan),
                onClick = { actions.onTogglePrepPlan(item.id) },
                modifier = semantic,
                leadingIcon = HhIcons.Check,
            )
        } else {
            HhOutlineButton(
                label = stringResource(R.string.feature_analysis_impl_add_to_prep_plan),
                onClick = { actions.onTogglePrepPlan(item.id) },
                modifier = semantic,
            )
        }
    }
    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            iHaveThis(Modifier.fillMaxWidth())
            prepPlan(Modifier.fillMaxWidth())
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            iHaveThis(Modifier.weight(1f))
            prepPlan(Modifier.weight(PREP_BUTTON_WEIGHT))
        }
    }
}

@Composable
internal fun SourceFactChip(factId: String, onClick: () -> Unit) {
    val colors = HhTheme.colors
    val description = stringResource(R.string.feature_analysis_impl_source_fact, factId)
    val underline = HhTheme.spacing.xxs
    Box(
        modifier = Modifier
            .heightIn(min = HhTheme.spacing.touch)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .height(HhTheme.spacing.d32 - HhTheme.spacing.d2)
                .clip(HhTheme.shapes.pill)
                .background(colors.evidence)
                .drawBehind {
                    val y = size.height - underline.toPx() / 2f
                    drawLine(colors.evidenceLine, Offset(0f, y), Offset(size.width, y), underline.toPx())
                }
                .padding(horizontal = HhTheme.spacing.md),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = factId, style = HhTheme.typography.factId, color = colors.onSurface)
        }
    }
}

@Composable
private fun rowDescription(item: RequirementItem, status: String, priority: String): String {
    val base = stringResource(R.string.feature_analysis_impl_row_description, item.requirement.text, priority, status)
    return when {
        item.isGap && !item.hasSource ->
            base + " " + stringResource(R.string.feature_analysis_impl_row_actions)
        item.factRefs.isNotEmpty() ->
            base + " " + stringResource(
                R.string.feature_analysis_impl_row_sources,
                item.factRefs.joinToString { it.displayId },
            )
        else -> base
    }
}

private fun MatchStatus.statusKind(): HhStatusKind = when (this) {
    MatchStatus.MET -> HhStatusKind.Met
    MatchStatus.PARTIAL -> HhStatusKind.Partial
    MatchStatus.GAP -> HhStatusKind.Gap
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

private const val STACK_FONT_SCALE = 1.3f
private const val HOP_FROM_SCALE = 0.94f
private const val PREP_BUTTON_WEIGHT = 1.2f
