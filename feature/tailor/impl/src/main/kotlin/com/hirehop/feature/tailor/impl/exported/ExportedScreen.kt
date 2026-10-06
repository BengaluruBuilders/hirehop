package com.hirehop.feature.tailor.impl.exported

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.component.HhAccent
import com.hirehop.core.designsystem.component.HhApplicationStatusChip
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhIconActionBar
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOnColorChip
import com.hirehop.core.designsystem.component.HhPillRow
import com.hirehop.core.designsystem.component.HhPillRowStyle
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSolidCard
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhToastHost
import com.hirehop.core.designsystem.component.HhToastResult
import com.hirehop.core.designsystem.component.rememberHhToastState
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.ExportFormat
import com.hirehop.core.ui.ApplicationStatusKindMapper
import com.hirehop.core.ui.component.ApplicationStatusSheet
import com.hirehop.core.ui.component.applicationStatusOptions
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.credits.CreditCounter
import com.hirehop.feature.tailor.impl.jobLine

@Composable
internal fun ExportedScreen(
    uiState: ExportedUiState,
    actions: ExportedActions,
    modifier: Modifier = Modifier,
) {
    val toastState = rememberHhToastState()
    val undoStatus = uiState.undoStatus
    val markedMessage = undoStatus?.let {
        stringResource(R.string.feature_tailor_impl_exported_marked_message, uiState.status.label())
    }
    val undoLabel = stringResource(R.string.feature_tailor_impl_exported_undo)
    LaunchedEffect(undoStatus) {
        if (undoStatus == null || markedMessage == null) return@LaunchedEffect
        when (toastState.show(message = markedMessage, actionLabel = undoLabel)) {
            HhToastResult.ActionPerformed -> actions.onUndoStatus()
            HhToastResult.Dismissed -> actions.onDismissUndo()
        }
    }
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
                title = stringResource(R.string.feature_tailor_impl_exported_title),
                subtitle = jobLine(uiState.jobTitle, uiState.jobCompany),
                onBack = actions.onNavigateBack,
                backContentDescription = stringResource(
                    R.string.feature_tailor_impl_exported_navigation_back_description,
                ),
            )
        },
        bottomBar = { ExportedBottomBar(uiState, actions) },
        snackbarHost = { HhToastHost(toastState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = HhTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            when (uiState.stage) {
                ExportedStage.LOADING -> ExportedLoading()
                ExportedStage.READY -> ExportedReady(uiState = uiState, actions = actions)
                ExportedStage.NO_APPLICATION,
                ExportedStage.NO_FILE,
                -> ExportedMissing()
            }
        }
    }
    if (uiState.statusSheetOpen) {
        ExportedStatusSheet(uiState = uiState, actions = actions)
    }
}

@Composable
private fun ExportedBottomBar(uiState: ExportedUiState, actions: ExportedActions) {
    if (uiState.stage == ExportedStage.READY && uiState.canUseFile) {
        HhIconActionBar(
            secondaryIcon = HhIcons.Share,
            secondaryContentDescription = stringResource(R.string.feature_tailor_impl_exported_share_action),
            onSecondaryClick = actions.onShare,
            primaryLabel = stringResource(R.string.feature_tailor_impl_exported_done),
            onPrimaryClick = actions.onDone,
            primaryTrailingIcon = HhIcons.Check,
        )
        return
    }
    HhBottomActionBar {
        if (uiState.stage == ExportedStage.READY) {
            HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_exported_done),
                onClick = actions.onDone,
                modifier = Modifier.weight(1f),
                trailingIcon = HhIcons.Check,
            )
        } else {
            HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_exported_error_back),
                onClick = actions.onNavigateBack,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ExportedLoading() {
    val loading = stringResource(R.string.feature_tailor_impl_exported_loading)
    Row(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhLoadingWheel(contentDesc = loading)
        Text(text = loading, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
    }
}

@Composable
private fun ExportedMissing() {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.xl)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            HhSpotIllustration(kind = HhSpotKind.Empty)
            Text(
                text = stringResource(R.string.feature_tailor_impl_exported_error_title),
                style = HhTheme.typography.titleL,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_exported_error_body),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
        }
    }
}

@Composable
private fun ExportedReady(
    uiState: ExportedUiState,
    actions: ExportedActions,
) {
    ExportedHeadlineCard(uiState)
    ExportedCreditCard(uiState)
    ExportedFileCard(uiState, actions)
    ExportedStatusCard(uiState, actions)
    ExportedNextSteps(actions)
}

@Composable
private fun ExportedHeadlineCard(uiState: ExportedUiState) {
    val headline = stringResource(R.string.feature_tailor_impl_exported_headline)
    val savedLine = jobLine(uiState.jobTitle, uiState.jobCompany)
    HhSolidCard(
        accent = HhAccent.Coral,
        monogram = uiState.jobCompany.trim().ifBlank { headline }.take(2),
        title = headline,
        subtitle = if (savedLine == null) {
            stringResource(R.string.feature_tailor_impl_exported_saved_line_bare)
        } else {
            stringResource(R.string.feature_tailor_impl_exported_saved_line, savedLine)
        },
        chips = { HhOnColorChip(label = headline) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExportedCreditCard(uiState: ExportedUiState) {
    if (!uiState.creditsKnown) return
    val largeFont = LocalDensity.current.fontScale >= LARGE_FONT_SCALE
    val left = uiState.creditsLeft
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.gutter)) {
        if (uiState.usesFreeCredit) {
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_exported_credit_free_title),
                    style = HhTheme.typography.titleM,
                    color = HhTheme.colors.onSurface,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.feature_tailor_impl_exported_credit_free_detail,
                        left,
                        left,
                    ),
                    style = HhTheme.typography.labelM,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        } else if (largeFont) {
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
                ExportedCreditCounter(left, uiState.creditsBefore)
                ExportedPaidCreditDetail(uiState)
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                itemVerticalAlignment = Alignment.Bottom,
            ) {
                ExportedCreditCounter(left, uiState.creditsBefore)
                ExportedPaidCreditDetail(uiState)
            }
        }
    }
}

@Composable
private fun ExportedCreditCounter(left: Int, previous: Int) {
    CreditCounter(
        credits = left,
        previousCredits = previous,
        suffix = stringResource(R.string.feature_tailor_impl_exported_credit_left),
        contentDescription = pluralStringResource(
            R.plurals.feature_tailor_impl_exported_credit_left_description,
            left,
            left,
        ),
    )
}

@Composable
private fun ExportedPaidCreditDetail(uiState: ExportedUiState) {
    Text(
        text = stringResource(
            if (uiState.creditsNeverExpire) {
                R.string.feature_tailor_impl_exported_credit_paid_detail_never_expire
            } else {
                R.string.feature_tailor_impl_exported_credit_paid_detail
            },
            uiState.creditsBefore,
        ),
        style = HhTheme.typography.labelM,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun ExportedFileCard(
    uiState: ExportedUiState,
    actions: ExportedActions,
) {
    if (uiState.canUseFile) {
        HhPillRow(
            title = uiState.fileName,
            onClick = actions.onOpen,
            style = HhPillRowStyle.Neutral,
            subtitle = fileDetail(uiState),
            icon = HhIcons.Description,
            trailingIcon = HhIcons.OpenInNew,
            titleMaxLines = 2,
        )
        return
    }
    HhCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = uiState.fileName,
            style = HhTheme.typography.factId,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_exported_share_missing),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.body,
        )
    }
}

@Composable
private fun fileDetail(uiState: ExportedUiState): String {
    val pages = uiState.pageCount?.let { count ->
        pluralStringResource(R.plurals.feature_tailor_impl_exported_file_pages, count, count)
    }
    val format = stringResource(
        when (uiState.format) {
            ExportFormat.PDF -> R.string.feature_tailor_impl_exported_format_pdf
            ExportFormat.DOCX -> R.string.feature_tailor_impl_exported_format_docx
        },
    )
    return listOfNotNull(pages, format, uiState.templateName).joinToString(" · ")
}

@Composable
private fun ExportedStatusCard(
    uiState: ExportedUiState,
    actions: ExportedActions,
) {
    if (uiState.asksForStatus) {
        HhPillRow(
            title = stringResource(R.string.feature_tailor_impl_exported_status_apply_action),
            onClick = actions.onOpenStatusSheet,
            style = HhPillRowStyle.Marigold,
            subtitle = stringResource(R.string.feature_tailor_impl_exported_status_question),
            icon = HhIcons.Check,
        )
        return
    }
    HhCard(modifier = Modifier.fillMaxWidth()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            HhApplicationStatusChip(
                kind = ApplicationStatusKindMapper().kindOf(uiState.status),
                label = uiState.status.label(),
            )
            uiState.markedOn?.let { date ->
                Text(
                    text = stringResource(R.string.feature_tailor_impl_exported_marked_on, date),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.onSurface,
                )
            }
        }
        HhTextButton(
            label = stringResource(R.string.feature_tailor_impl_exported_status_change_action),
            onClick = actions.onOpenStatusSheet,
        )
    }
}

@Composable
private fun ExportedNextSteps(actions: ExportedActions) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhPillRow(
            title = stringResource(R.string.feature_tailor_impl_exported_next_prep_questions),
            onClick = actions.onGetPrepQuestions,
            style = HhPillRowStyle.Coral,
            icon = HhIcons.Description,
        )
        HhPillRow(
            title = stringResource(R.string.feature_tailor_impl_exported_next_cover_letter),
            onClick = actions.onWriteCoverLetter,
            style = HhPillRowStyle.Neutral,
            icon = HhIcons.Description,
        )
    }
}

@Composable
private fun ExportedStatusSheet(
    uiState: ExportedUiState,
    actions: ExportedActions,
) {
    val labels = ApplicationStatus.entries.associateWith { status -> status.label() }
    val options = applicationStatusOptions { status -> labels.getValue(status) }
    ApplicationStatusSheet(
        current = if (uiState.asksForStatus) ApplicationStatus.APPLIED else uiState.status,
        options = options,
        onConfirm = actions.onConfirmStatus,
        onDismiss = actions.onDismissStatusSheet,
        saveLabel = stringResource(R.string.feature_tailor_impl_exported_status_sheet_save),
        cancelLabel = stringResource(R.string.feature_tailor_impl_exported_status_sheet_cancel),
        title = stringResource(R.string.feature_tailor_impl_exported_status_question),
        note = jobLine(uiState.jobTitle, uiState.jobCompany)?.let { line ->
            stringResource(R.string.feature_tailor_impl_exported_status_sheet_note, line)
        },
    )
}

@Composable
private fun ApplicationStatus.label(): String = stringResource(
    when (this) {
        ApplicationStatus.SAVED -> R.string.feature_tailor_impl_exported_status_saved
        ApplicationStatus.APPLIED -> R.string.feature_tailor_impl_exported_status_applied
        ApplicationStatus.INTERVIEW -> R.string.feature_tailor_impl_exported_status_interview
        ApplicationStatus.OFFER -> R.string.feature_tailor_impl_exported_status_offer
        ApplicationStatus.REJECTED -> R.string.feature_tailor_impl_exported_status_rejected
        ApplicationStatus.NO_RESPONSE -> R.string.feature_tailor_impl_exported_status_no_response
    },
)

private const val LARGE_FONT_SCALE = 1.5f
