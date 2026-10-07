package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hirehop.core.data.repository.UsageAllowance
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhMonogram
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

private enum class WaitingPillKind { Done, InProgress, UpNext }

@Composable
internal fun WaitingContent(state: AnalysisUiState.Analyzing, contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.gutter)
            .padding(top = contentPadding.calculateTopPadding() + HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        JobCard(state)
        listOf(
            stringResource(R.string.feature_analysis_impl_step_read),
            stringResource(R.string.feature_analysis_impl_step_match),
            stringResource(R.string.feature_analysis_impl_step_sort),
        ).forEachIndexed { position, title ->
            val kind = pillKind(position = position, current = state.stepIndex)
            WaitingPill(title = title, stateLabel = stringResource(kind.stateLabelRes()), kind = kind)
        }
        Text(
            text = stringResource(R.string.feature_analysis_impl_waiting_duration),
            modifier = Modifier.fillMaxWidth().padding(top = HhTheme.spacing.sm),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.feature_analysis_impl_waiting_footnote),
            modifier = Modifier.fillMaxWidth(),
            style = HhTheme.typography.bodyS,
            color = HhTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun JobCard(state: AnalysisUiState) {
    val title = state.headerTitle()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HhTheme.colors.card, HhTheme.shapes.card)
            .padding(horizontal = HhTheme.spacing.lg, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhMonogram(text = state.job.company.ifBlank { title }, size = 44.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
            state.headerSubtitle()?.let {
                Text(text = it, style = HhTheme.typography.bodyS, color = HhTheme.colors.onSurfaceVariant)
            }
        }
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
            .defaultMinSize(minHeight = 56.dp)
            .background(colors.card, HhTheme.shapes.statusRow)
            .padding(horizontal = HhTheme.spacing.lg, vertical = 14.dp)
            .semantics(mergeDescendants = true) { contentDescription = "$title. $stateLabel" },
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WaitingMark(kind)
        Text(
            text = title,
            style = HhTheme.typography.titleS,
            color = if (kind == WaitingPillKind.UpNext) colors.onSurfaceVariant else colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(text = stateLabel, style = HhTheme.typography.labelM, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun WaitingMark(kind: WaitingPillKind) {
    val colors = HhTheme.colors
    val mark = Modifier.size(24.dp)
    when (kind) {
        WaitingPillKind.Done -> Box(mark.background(colors.brand, CircleShape), contentAlignment = Alignment.Center) {
            Icon(HhIcons.Check, contentDescription = null, tint = colors.onBrand, modifier = Modifier.size(16.dp))
        }
        WaitingPillKind.InProgress -> Box(mark.border(2.dp, colors.primary, CircleShape))
        WaitingPillKind.UpNext -> Box(mark.border(2.dp, colors.outlineVariant, CircleShape))
    }
}

@Composable
private fun InfoNote(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = HhIcons.Info,
            contentDescription = null,
            tint = HhTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(text = text, style = HhTheme.typography.bodyS, color = HhTheme.colors.onSurfaceVariant)
    }
}

@Composable
internal fun MessageContent(
    title: String,
    body: String,
    contentPadding: PaddingValues,
    chip: String? = null,
    note: String? = null,
) {
    val isError = chip == null
    val colors = HhTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.gutter)
            .padding(top = contentPadding.calculateTopPadding() + HhTheme.spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(if (isError) colors.errorContainer else colors.partialContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isError) HhIcons.Error else HhIcons.Clock,
                contentDescription = null,
                tint = if (isError) colors.error else colors.partial,
                modifier = Modifier.size(40.dp),
            )
        }
        HhHeadline(
            text = title,
            style = HhTheme.typography.headlineL.copy(textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = body,
            style = HhTheme.typography.bodyL,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        chip?.let { HhStatusChip(kind = HhStatusKind.Gap, label = it) }
        note?.let { InfoNote(it) }
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
                HhBottomActionBar(stacked = true, primaryLast = false) {
                    HhPrimaryButton(
                        label = stringResource(R.string.feature_analysis_impl_retry),
                        onClick = actions.onRetry,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HhSecondaryButton(
                        label = stringResource(R.string.feature_analysis_impl_back_to_job),
                        onClick = actions.onBackToJobDescription,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        is AnalysisUiState.DailyLimit -> {
            {
                HhBottomActionBar {
                    HhSecondaryButton(
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
            label = stringResource(
                if (state.isOffline) R.string.feature_analysis_impl_tailor_offline else R.string.feature_analysis_impl_tailor,
            ),
            onClick = actions.onTailor,
            modifier = Modifier.weight(1f),
            enabled = state.canTailor,
            trailingIcon = HhIcons.ArrowForward,
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
                    pluralStringResource(
                        R.plurals.feature_analysis_impl_credit_note,
                        state.totalCredits,
                        state.totalCredits,
                    ),
                    state.totalCredits.toString(),
                ),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@Composable
internal fun NoteCard(icon: ImageVector, content: @Composable () -> Unit) {
    val colors = HhTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, HhTheme.shapes.banner)
            .padding(horizontal = HhTheme.spacing.lg, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.onSurface,
            modifier = Modifier.size(NOTE_ICON_SIZE),
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

private val NOTE_ICON_SIZE = 22.dp

private fun boldNumber(text: String, number: String): AnnotatedString = buildAnnotatedString {
    append(text)
    val start = text.lastIndexOf(number)
    if (start >= 0) addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, start + number.length)
}
