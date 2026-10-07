package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.core.data.repository.UsageAllowance
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

private enum class WaitingPillKind { Done, InProgress, UpNext }

@Composable
internal fun WaitingContent(state: AnalysisUiState.Analyzing, contentPadding: PaddingValues) {
    val stepIndex = state.stepIndex
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = HhTheme.spacing.gutter)
            .padding(top = contentPadding.calculateTopPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_analysis_impl_waiting_heading),
            style = HhTheme.typography.titleL,
            color = HhTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val readTitle = if (state.requirementCount != null && stepIndex > 0) {
                pluralStringResource(
                    R.plurals.feature_analysis_impl_step_read_done,
                    state.requirementCount,
                    state.requirementCount,
                )
            } else {
                stringResource(R.string.feature_analysis_impl_step_read)
            }
            val matchTitle = if (state.factCount > 0) {
                pluralStringResource(
                    R.plurals.feature_analysis_impl_step_match_count,
                    state.factCount,
                    state.factCount,
                )
            } else {
                stringResource(R.string.feature_analysis_impl_step_match)
            }
            listOf(
                readTitle,
                matchTitle,
                stringResource(R.string.feature_analysis_impl_step_sort),
            ).forEachIndexed { position, title ->
                val kind = pillKind(position = position, current = stepIndex)
                WaitingPill(title = title, stateLabel = stringResource(kind.stateLabelRes()), kind = kind)
            }
        }
        WaitingFootnote(text = stringResource(R.string.feature_analysis_impl_waiting_footnote))
    }
}

private fun pillKind(position: Int, current: Int): WaitingPillKind = when {
    position < current -> WaitingPillKind.Done
    position == current -> WaitingPillKind.InProgress
    else -> WaitingPillKind.UpNext
}

@StringRes
private fun WaitingPillKind.stateLabelRes(): Int = when (this) {
    WaitingPillKind.Done -> R.string.feature_analysis_impl_step_state_done
    WaitingPillKind.InProgress -> R.string.feature_analysis_impl_step_state_in_progress
    WaitingPillKind.UpNext -> R.string.feature_analysis_impl_step_state_up_next
}

@Composable
private fun WaitingPill(title: String, stateLabel: String, kind: WaitingPillKind) {
    val colors = HhTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(
                color = when (kind) {
                    WaitingPillKind.Done -> colors.brand
                    WaitingPillKind.InProgress -> colors.header
                    WaitingPillKind.UpNext -> colors.card
                },
                shape = HhTheme.shapes.pill,
            )
            .then(
                if (kind == WaitingPillKind.UpNext) {
                    Modifier.border(1.dp, colors.outlineVariant, HhTheme.shapes.pill)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 12.dp)
            .semantics(mergeDescendants = true) { contentDescription = "$title. $stateLabel" },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (kind == WaitingPillKind.InProgress) colors.onHeader.copy(alpha = 0.12f) else colors.surface,
                    HhTheme.shapes.pill,
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (kind) {
                WaitingPillKind.Done -> Icon(
                    imageVector = HhIcons.Check,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp),
                )
                WaitingPillKind.InProgress -> Icon(
                    imageVector = HhIcons.Clock,
                    contentDescription = null,
                    tint = colors.onHeader,
                    modifier = Modifier.size(24.dp),
                )
                WaitingPillKind.UpNext -> Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, colors.outlineVariant, HhTheme.shapes.pill),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
        ) {
            Text(
                text = title,
                style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.Bold),
                color = when (kind) {
                    WaitingPillKind.Done -> colors.onBrand
                    WaitingPillKind.InProgress -> colors.onHeader
                    WaitingPillKind.UpNext -> colors.onSurface
                },
            )
            Text(
                text = stateLabel,
                style = HhTheme.typography.labelM,
                color = when (kind) {
                    WaitingPillKind.Done -> colors.onBrand
                    WaitingPillKind.InProgress -> colors.onHeaderVariant
                    WaitingPillKind.UpNext -> colors.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun WaitingFootnote(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = HhIcons.Info,
            contentDescription = null,
            tint = HhTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = HhTheme.typography.labelL,
            color = HhTheme.colors.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun MessageContent(
    title: String,
    body: String,
    contentPadding: PaddingValues,
    note: String? = null,
) {
    val isError = note == null
    val colors = HhTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = HhTheme.spacing.gutter)
            .padding(top = contentPadding.calculateTopPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = HhTheme.typography.titleL,
            color = colors.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.card, HhTheme.shapes.card)
                .border(1.dp, colors.outlineVariant, HhTheme.shapes.card)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colors.surface, HhTheme.shapes.pill)
                    .border(1.dp, colors.outlineVariant, HhTheme.shapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isError) HhIcons.Error else HhIcons.Clock,
                    contentDescription = null,
                    tint = if (isError) colors.error else colors.gap,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = body,
                style = HhTheme.typography.bodyL,
                color = colors.body,
                modifier = Modifier.weight(1f),
            )
        }
        if (note != null) WaitingFootnote(text = note)
    }
}

internal fun analysisBottomBar(uiState: AnalysisUiState, actions: AnalysisActions): (@Composable () -> Unit)? =
    when (uiState) {
        is AnalysisUiState.Result -> if (uiState.hasKeyTerms) {
            { ResultBottomBar(uiState, actions) }
        } else {
            {
                HhBottomActionBar {
                    HhPrimaryButton(
                        label = stringResource(R.string.feature_analysis_impl_edit_job),
                        onClick = actions.onBackToJobDescription,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        is AnalysisUiState.Failed -> {
            {
                HhBottomActionBar {
                    HhPrimaryButton(
                        label = stringResource(R.string.feature_analysis_impl_retry),
                        onClick = actions.onRetry,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        is AnalysisUiState.DailyLimit -> {
            {
                HhBottomActionBar {
                    HhOutlineButton(
                        label = stringResource(R.string.feature_analysis_impl_back_to_job),
                        onClick = actions.onBackToJobDescription,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        else -> null
    }

internal fun analysisBottomBarNotice(uiState: AnalysisUiState): (@Composable () -> Unit)? =
    (uiState as? AnalysisUiState.Result)?.takeIf { it.hasKeyTerms }?.let { result -> { BarNote(result) } }

@Composable
private fun ResultBottomBar(state: AnalysisUiState.Result, actions: AnalysisActions) {
    HhBottomActionBar {
        HhPrimaryButton(
            label = stringResource(R.string.feature_analysis_impl_tailor),
            onClick = actions.onTailor,
            modifier = Modifier.weight(1f),
            enabled = state.canTailor,
        )
    }
}

@Composable
private fun BarNote(state: AnalysisUiState.Result) {
    when {
        state.isOffline -> Text(
            text = stringResource(R.string.feature_analysis_impl_offline_caption),
            modifier = Modifier.fillMaxWidth().padding(horizontal = HhTheme.spacing.sm),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurface,
        )
        state.tailorLimitReached -> NoteCard(icon = HhIcons.Clock) {
            Text(
                text = stringResource(R.string.feature_analysis_impl_tailor_limit),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(
                    R.string.feature_analysis_impl_tailor_limit_cap,
                    UsageAllowance.DAILY_FREE_TAILORINGS,
                ),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        else -> NoteCard(icon = HhIcons.Download) {
            Text(
                text = boldNumber(
                    stringResource(R.string.feature_analysis_impl_credit_note, state.freeCredits.toString()),
                    state.freeCredits.toString(),
                ),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun NoteCard(icon: ImageVector, content: @Composable () -> Unit) {
    val colors = HhTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, HhTheme.shapes.card)
            .border(HhTheme.spacing.d2 / 2, colors.outlineVariant, HhTheme.shapes.card)
            .padding(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.sm + HhTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.xxs),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.padding(top = HhTheme.spacing.xxs).size(NOTE_ICON_SIZE),
        )
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) { content() }
    }
}

@Composable
internal fun toastText(toast: AnalysisToast): String = when (toast) {
    is AnalysisToast.PrepAdded ->
        stringResource(R.string.feature_analysis_impl_toast_prep_added, toast.requirementText.headline())
    AnalysisToast.GapClosed -> stringResource(R.string.feature_analysis_impl_toast_gap_closed)
    AnalysisToast.Reported -> stringResource(R.string.feature_analysis_impl_toast_reported)
    AnalysisToast.EvidenceFailed -> stringResource(R.string.feature_analysis_impl_toast_evidence_failed)
    AnalysisToast.TailorFailed -> stringResource(R.string.feature_analysis_impl_toast_tailor_failed)
}

private val NOTE_ICON_SIZE = 18.dp

private fun boldNumber(text: String, number: String): AnnotatedString = buildAnnotatedString {
    append(text)
    val start = text.lastIndexOf(number)
    if (start >= 0) addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, start + number.length)
}
