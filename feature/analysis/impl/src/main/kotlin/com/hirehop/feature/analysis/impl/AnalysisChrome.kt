package com.hirehop.feature.analysis.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.core.data.repository.UsageAllowance
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
internal fun WaitingContent(state: AnalysisUiState.Analyzing, contentPadding: PaddingValues) {
    val readDetail = if (state.keyTermCount != null && state.requirementCount != null) {
        pluralStringResource(
            R.plurals.feature_analysis_impl_step_read_detail_terms,
            state.keyTermCount,
            state.keyTermCount,
        ) + ", " + pluralStringResource(
            R.plurals.feature_analysis_impl_step_read_detail_requirements,
            state.requirementCount,
            state.requirementCount,
        )
    } else {
        null
    }
    val matchDetail = if (state.factCount > 0) {
        pluralStringResource(R.plurals.feature_analysis_impl_step_match_detail, state.factCount, state.factCount)
    } else {
        null
    }
    Column(Modifier.fillMaxSize()) {
        HhStepProgress(
            stepNames = listOf(
                stringResource(R.string.feature_analysis_impl_step_read),
                stringResource(R.string.feature_analysis_impl_step_match),
                stringResource(R.string.feature_analysis_impl_step_sort),
            ),
            currentStepIndex = state.stepIndex,
            modifier = Modifier
                .padding(horizontal = HhTheme.spacing.gutter)
                .padding(top = contentPadding.calculateTopPadding()),
            ordinalLabel = stringResource(R.string.feature_analysis_impl_waiting_limit),
            stepDetails = listOf(readDetail, matchDetail, null),
            footnote = stringResource(R.string.feature_analysis_impl_waiting_footnote),
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = HhTheme.spacing.gutter)
            .padding(top = contentPadding.calculateTopPadding()),
    ) {
        HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.cardPadding + HhTheme.spacing.xs)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (isError) HhIcons.Error else HhIcons.Clock,
                    contentDescription = null,
                    tint = if (isError) HhTheme.colors.error else HhTheme.colors.gap,
                    modifier = Modifier.size(HhTheme.spacing.xxl),
                )
                Text(text = title, style = HhTheme.typography.titleL, color = HhTheme.colors.onSurface)
            }
            Text(text = body, style = HhTheme.typography.bodyL, color = HhTheme.colors.body)
            if (note != null) {
                Text(text = note, style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurfaceVariant)
            }
        }
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
        HhOutlineButton(
            label = stringResource(R.string.feature_analysis_impl_share_card),
            onClick = actions.onOpenShareCard,
            modifier = Modifier.weight(1f),
            trailingIcon = HhIcons.Share,
        )
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
