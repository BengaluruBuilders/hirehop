package com.tailormyresume.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.tailormyresume.core.designsystem.component.TmrButtonSize
import com.tailormyresume.core.designsystem.component.TmrFactId
import com.tailormyresume.core.designsystem.component.TmrIconButton
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrProvenanceChip
import com.tailormyresume.core.designsystem.component.TmrProvenanceKind
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrStatusChip
import com.tailormyresume.core.designsystem.component.TmrStatusKind
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.MatchStatus

@Composable
internal fun RequirementRow(
    item: RequirementItem,
    isClosed: Boolean,
    actions: AnalysisActions,
    onMenuAnchor: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hop = remember { Animatable(1f) }
    val hopSpec = TmrTheme.motion.hopSpecs.scale
    LaunchedEffect(isClosed) {
        if (isClosed) {
            hop.snapTo(HOP_FROM_SCALE)
            hop.animateTo(1f, hopSpec)
        }
    }
    val statusLabel = stringResource(item.status.labelRes())
    val priorityLabel = stringResource(item.priorityLabelRes())
    val description = rowDescription(item, statusLabel, priorityLabel)
    val shape = if (item.isGap) TmrTheme.shapes.card else TmrTheme.shapes.statusRow
    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = hop.value
                scaleY = hop.value
            }
            .semantics { contentDescription = description }
            .clip(shape)
            .background(TmrTheme.colors.card)
            .then(
                if (item.hasSource) {
                    Modifier.clickable(role = Role.Button) { actions.onSeeSource(item.id) }
                } else {
                    Modifier
                },
            )
            .padding(
                horizontal = TmrTheme.spacing.lg,
                vertical = if (item.isGap) TmrTheme.spacing.lg else TmrTheme.spacing.md,
            ),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm + TmrTheme.spacing.xxs),
    ) {
        if (item.isGap) {
            GapBody(item, statusLabel, actions, onMenuAnchor)
        } else {
            FoundBody(item, statusLabel, actions, onMenuAnchor)
        }
    }
}

@Composable
private fun GapBody(
    item: RequirementItem,
    statusLabel: String,
    actions: AnalysisActions,
    onMenuAnchor: (Rect) -> Unit,
) {
    val name = item.requirement.gapName()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name,
            style = TmrTheme.typography.titleM,
            color = TmrTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        RowMenuButton(item, actions, onMenuAnchor)
    }
    TmrStatusChip(kind = TmrStatusKind.Gap, label = statusLabel)
    (if (item.requirement.text.headline() != name) item.requirement.text.trim() else item.requirement.text.splitDetail().second)?.let { askedFor ->
        Text(
            text = stringResource(R.string.feature_analysis_impl_asked_for, askedFor),
            style = TmrTheme.typography.bodyS,
            color = TmrTheme.colors.onSurfaceVariant,
        )
    }
    if (!item.hasSource) GapActions(item, actions)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoundBody(
    item: RequirementItem,
    statusLabel: String,
    actions: AnalysisActions,
    onMenuAnchor: (Rect) -> Unit,
) {
    Row(
        modifier = Modifier.heightIn(min = TmrTheme.spacing.d32 + TmrTheme.spacing.d12),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs)) {
            Text(text = item.requirement.text.headline(), style = TmrTheme.typography.titleM, color = TmrTheme.colors.onSurface)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                item.factRefs.forEach { TmrFactId(id = it.displayId) }
                item.sourceLabel()?.let {
                    Text(text = it, style = TmrTheme.typography.bodyS, color = TmrTheme.colors.onSurfaceVariant)
                }
                if (item.userStatedSkills.isNotEmpty()) {
                    TmrProvenanceChip(
                        kind = TmrProvenanceKind.UserStated,
                        label = stringResource(R.string.feature_analysis_impl_provenance_user_stated),
                    )
                }
            }
        }
        TmrStatusChip(kind = item.status.statusKind(), label = statusLabel)
        RowMenuButton(item, actions, onMenuAnchor)
    }
}

@Composable
private fun RowMenuButton(item: RequirementItem, actions: AnalysisActions, onMenuAnchor: (Rect) -> Unit) {
    if (!item.hasMenu) return
    TmrIconButton(
        icon = TmrIcons.More,
        contentDescription = stringResource(R.string.feature_analysis_impl_more_actions),
        onClick = { actions.onOpenMenu(item.id) },
        modifier = Modifier.onGloballyPositioned { onMenuAnchor(it.boundsInRoot()) },
        tint = TmrTheme.colors.onSurfaceVariant,
        containerColor = Color.Transparent,
        borderColor = Color.Transparent,
    )
}

private fun RequirementItem.sourceLabel(): String? =
    factRefs.firstOrNull()?.let { listOf(it.title, it.organization).filter(String::isNotBlank).joinToString(", ") }
        ?.takeIf(String::isNotBlank)
        ?: skills.joinToString().takeIf(String::isNotBlank)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GapActions(item: RequirementItem, actions: AnalysisActions) {
    val stacked = LocalDensity.current.fontScale >= STACK_FONT_SCALE
    val prepDescription = stringResource(R.string.feature_analysis_impl_prep_description, item.requirement.text)
    val prepState = stringResource(
        if (item.isInPrepPlan) R.string.feature_analysis_impl_prep_added else R.string.feature_analysis_impl_prep_not_added,
    )
    val iHaveThis: @Composable (Modifier) -> Unit = { buttonModifier ->
        TmrSecondaryButton(
            label = stringResource(R.string.feature_analysis_impl_i_have_this),
            onClick = { actions.onIHaveThis(item.id) },
            modifier = buttonModifier,
            leadingIcon = TmrIcons.Edit,
            size = TmrButtonSize.Compact,
        )
    }
    val prepPlan: @Composable (Modifier) -> Unit = { buttonModifier ->
        TmrOutlineButton(
            label = stringResource(R.string.feature_analysis_impl_add_to_prep_plan),
            onClick = { actions.onTogglePrepPlan(item.id) },
            modifier = buttonModifier.semantics {
                contentDescription = prepDescription
                stateDescription = prepState
                role = Role.Switch
            },
            leadingIcon = if (item.isInPrepPlan) TmrIcons.Check else TmrIcons.Add,
            size = TmrButtonSize.Compact,
        )
    }
    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
            iHaveThis(Modifier.fillMaxWidth())
            prepPlan(Modifier.fillMaxWidth())
        }
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            iHaveThis(Modifier)
            prepPlan(Modifier)
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

private fun MatchStatus.statusKind(): TmrStatusKind = when (this) {
    MatchStatus.MET -> TmrStatusKind.Met
    MatchStatus.PARTIAL -> TmrStatusKind.Partial
    MatchStatus.GAP -> TmrStatusKind.Gap
}

@StringRes
private fun MatchStatus.labelRes(): Int = when (this) {
    MatchStatus.MET -> R.string.feature_analysis_impl_status_met
    MatchStatus.PARTIAL -> R.string.feature_analysis_impl_status_partial
    MatchStatus.GAP -> R.string.feature_analysis_impl_status_gap
}

@StringRes
internal fun RequirementItem.priorityLabelRes(): Int = if (isMustHave) {
    R.string.feature_analysis_impl_priority_must_have
} else {
    R.string.feature_analysis_impl_priority_nice_to_have
}

private const val STACK_FONT_SCALE = 1.3f
private const val HOP_FROM_SCALE = 0.94f
