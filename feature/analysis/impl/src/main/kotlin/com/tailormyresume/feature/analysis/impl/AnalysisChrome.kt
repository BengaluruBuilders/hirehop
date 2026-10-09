package com.tailormyresume.feature.analysis.impl

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.data.repository.UsageAllowance
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrMonogram
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrStatusChip
import com.tailormyresume.core.designsystem.component.TmrStatusKind
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

internal const val JOB_CARD_TAG = "analysis-job-card"

private enum class WaitingPillKind { Done, InProgress, UpNext }

@Composable
internal fun WaitingContent(
    state: AnalysisUiState.Analyzing,
    contentPadding: PaddingValues,
    jobKnown: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TmrTheme.spacing.gutter)
            .padding(top = contentPadding.calculateTopPadding() + TmrTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        JobCard(
            state,
            modifier = Modifier.testTag(JOB_CARD_TAG).then(if (jobKnown) Modifier else Modifier.alpha(0f).clearAndSetSemantics {}),
        )
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
            modifier = Modifier.fillMaxWidth().padding(top = TmrTheme.spacing.sm),
            style = TmrTheme.typography.labelM,
            color = TmrTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.feature_analysis_impl_waiting_footnote),
            modifier = Modifier.fillMaxWidth(),
            style = TmrTheme.typography.bodyS,
            color = TmrTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun JobCard(state: AnalysisUiState, modifier: Modifier = Modifier) {
    val title = state.headerTitle()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TmrTheme.colors.card, TmrTheme.shapes.card)
            .padding(horizontal = TmrTheme.spacing.lg, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TmrMonogram(text = state.job.company.ifBlank { title }, size = 44.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = TmrTheme.typography.titleM, color = TmrTheme.colors.onSurface)
            state.headerSubtitle()?.let {
                Text(text = it, style = TmrTheme.typography.bodyS, color = TmrTheme.colors.onSurfaceVariant)
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
    val colors = TmrTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .background(colors.card, TmrTheme.shapes.statusRow)
            .padding(horizontal = TmrTheme.spacing.lg, vertical = 14.dp)
            .semantics(mergeDescendants = true) { contentDescription = "$title. $stateLabel" },
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WaitingMark(kind)
        Text(
            text = title,
            style = TmrTheme.typography.titleS,
            color = if (kind == WaitingPillKind.UpNext) colors.onSurfaceVariant else colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(text = stateLabel, style = TmrTheme.typography.labelM, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun WaitingMark(kind: WaitingPillKind) {
    val colors = TmrTheme.colors
    val mark = Modifier.size(24.dp)
    when (kind) {
        WaitingPillKind.Done -> Box(mark.background(colors.brand, CircleShape), contentAlignment = Alignment.Center) {
            Icon(TmrIcons.Check, contentDescription = null, tint = colors.onBrand, modifier = Modifier.size(16.dp))
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
            imageVector = TmrIcons.Info,
            contentDescription = null,
            tint = TmrTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(text = text, style = TmrTheme.typography.bodyS, color = TmrTheme.colors.onSurfaceVariant)
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
    val colors = TmrTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TmrTheme.spacing.gutter)
            .padding(top = contentPadding.calculateTopPadding() + TmrTheme.spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.lg),
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(if (isError) colors.errorContainer else colors.partialContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isError) TmrIcons.Error else TmrIcons.Clock,
                contentDescription = null,
                tint = if (isError) colors.error else colors.partial,
                modifier = Modifier.size(40.dp),
            )
        }
        TmrHeadline(
            text = title,
            style = TmrTheme.typography.headlineL.copy(textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = body,
            style = TmrTheme.typography.bodyL,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        chip?.let { TmrStatusChip(kind = TmrStatusKind.Gap, label = it) }
        note?.let { InfoNote(it) }
    }
}

internal fun analysisBottomBar(uiState: AnalysisUiState, actions: AnalysisActions): (@Composable () -> Unit)? =
    when (uiState) {
        is AnalysisUiState.Result -> if (uiState.hasKeyTerms) {
            { ResultBottomBar(uiState, actions) }
        } else {
            {
                TmrBottomActionBar {
                    TmrPrimaryButton(
                        label = stringResource(R.string.feature_analysis_impl_edit_job),
                        onClick = actions.onBackToJobDescription,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        is AnalysisUiState.Failed -> if (uiState.cause == FailureCause.QuotaReached) {
            {
                TmrBottomActionBar {
                    TmrSecondaryButton(
                        label = stringResource(R.string.feature_analysis_impl_back_to_job),
                        onClick = actions.onBackToJobDescription,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } else {
            {
                val signIn = uiState.cause == FailureCause.SignInRequired
                TmrBottomActionBar(stacked = true, primaryLast = false) {
                    TmrPrimaryButton(
                        label = stringResource(
                            if (signIn) R.string.feature_analysis_impl_sign_in_again else R.string.feature_analysis_impl_retry,
                        ),
                        onClick = if (signIn) actions.onSignInAgain else actions.onRetry,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TmrSecondaryButton(
                        label = stringResource(R.string.feature_analysis_impl_back_to_job),
                        onClick = actions.onBackToJobDescription,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        is AnalysisUiState.DailyLimit -> {
            {
                TmrBottomActionBar {
                    TmrSecondaryButton(
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
    (uiState as? AnalysisUiState.Result)?.takeIf { it.hasKeyTerms && it.tailorLimitReached && !it.isOffline }
        ?.let { { BarNote() } }

@Composable
private fun ResultBottomBar(state: AnalysisUiState.Result, actions: AnalysisActions) {
    TmrBottomActionBar {
        TmrPrimaryButton(
            label = stringResource(
                if (state.isOffline) R.string.feature_analysis_impl_tailor_offline else R.string.feature_analysis_impl_tailor,
            ),
            onClick = actions.onTailor,
            modifier = Modifier.weight(1f),
            enabled = state.canTailor,
            trailingIcon = TmrIcons.ArrowForward,
        )
    }
}

@Composable
private fun BarNote() {
    NoteCard(icon = TmrIcons.Clock) {
        Text(
            text = stringResource(R.string.feature_analysis_impl_tailor_limit),
            style = TmrTheme.typography.bodyM,
            color = TmrTheme.colors.onSurface,
        )
        Text(
            text = stringResource(
                R.string.feature_analysis_impl_tailor_limit_cap,
                UsageAllowance.DAILY_FREE_TAILORINGS,
            ),
            style = TmrTheme.typography.labelM,
            color = TmrTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
internal fun NoteCard(icon: ImageVector, content: @Composable () -> Unit) {
    val colors = TmrTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, TmrTheme.shapes.banner)
            .padding(horizontal = TmrTheme.spacing.lg, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.onSurface,
            modifier = Modifier.size(NOTE_ICON_SIZE),
        )
        Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xxs)) { content() }
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
