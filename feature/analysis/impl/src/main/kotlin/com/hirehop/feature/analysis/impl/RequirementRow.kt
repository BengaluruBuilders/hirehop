package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButtonSize
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhStatusDisc
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.MatchStatus

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
    val isMet = item.status == MatchStatus.MET
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = hop.value
                scaleY = hop.value
            }
            .semantics { contentDescription = description },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(HhTheme.shapes.card)
                .background(HhTheme.colors.card)
                .then(
                    if (isMet) {
                        Modifier
                    } else {
                        Modifier.border(1.dp, HhTheme.colors.outlineVariant, HhTheme.shapes.card)
                    },
                )
                .then(
                    if (item.hasSource) {
                        Modifier.clickable(role = Role.Button) { actions.onSeeSource(item.id) }
                    } else {
                        Modifier
                    },
                )
                .then(
                    if (isMet) {
                        Modifier
                            .heightIn(min = 64.dp)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    } else {
                        Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp)
                    },
                ),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    HhStatusDisc(kind = item.status.statusKind(), size = 26.dp)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .then(if (item.hasMenu) Modifier.padding(end = 48.dp) else Modifier),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = item.requirement.text,
                            style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.Bold),
                            color = HhTheme.colors.onSurface,
                        )
                        Text(
                            text = statusLineText(item, statusLabel),
                            style = HhTheme.typography.labelM,
                            color = when (item.status) {
                                MatchStatus.MET -> HhTheme.colors.met
                                MatchStatus.PARTIAL -> HhTheme.colors.partial
                                MatchStatus.GAP -> HhTheme.colors.body
                            },
                        )
                    }
                }
                if (item.isGap && !item.hasSource) {
                    GapActions(item, actions, Modifier.padding(start = 38.dp))
                }
            }
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

@Composable
private fun statusLineText(item: RequirementItem, statusLabel: String): String = when {
    item.factRefs.isNotEmpty() -> stringResource(
        R.string.feature_analysis_impl_row_line,
        statusLabel,
        item.factRefs.joinToString(" ") { it.displayId },
    )
    item.status == MatchStatus.GAP ->
        stringResource(R.string.feature_analysis_impl_row_line_no_fact, statusLabel)
    else -> statusLabel
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GapActions(item: RequirementItem, actions: AnalysisActions, modifier: Modifier = Modifier) {
    val stacked = LocalDensity.current.fontScale >= STACK_FONT_SCALE
    val prepDescription = stringResource(R.string.feature_analysis_impl_prep_description, item.requirement.text)
    val prepState = stringResource(
        if (item.isInPrepPlan) R.string.feature_analysis_impl_prep_added else R.string.feature_analysis_impl_prep_not_added,
    )
    val iHaveThis: @Composable (Modifier) -> Unit = { buttonModifier ->
        HhOutlineButton(
            label = stringResource(R.string.feature_analysis_impl_i_have_this),
            onClick = { actions.onIHaveThis(item.id) },
            modifier = buttonModifier.height(GAP_BUTTON_HEIGHT),
            size = HhButtonSize.Compact,
        )
    }
    val prepPlan: @Composable (Modifier) -> Unit = { buttonModifier ->
        HhSecondaryButton(
            label = stringResource(R.string.feature_analysis_impl_add_to_prep_plan),
            onClick = { actions.onTogglePrepPlan(item.id) },
            modifier = buttonModifier
                .height(GAP_BUTTON_HEIGHT)
                .semantics {
                    contentDescription = prepDescription
                    stateDescription = prepState
                    role = Role.Switch
                },
            leadingIcon = if (item.isInPrepPlan) HhIcons.Check else null,
            size = HhButtonSize.Compact,
        )
    }
    if (stacked) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            iHaveThis(Modifier.fillMaxWidth())
            prepPlan(Modifier.fillMaxWidth())
        }
    } else {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            iHaveThis(Modifier)
            prepPlan(Modifier)
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
private val GAP_BUTTON_HEIGHT = 44.dp
