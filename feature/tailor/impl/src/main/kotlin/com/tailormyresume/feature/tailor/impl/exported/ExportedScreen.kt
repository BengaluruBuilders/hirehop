package com.tailormyresume.feature.tailor.impl.exported

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrApplicationStatusChip
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrButtonSize
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrLoadingWheel
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrSpotKind
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.component.TmrToastHost
import com.tailormyresume.core.designsystem.component.TmrToastResult
import com.tailormyresume.core.designsystem.component.rememberTmrToastState
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.ui.ApplicationStatusKindMapper
import com.tailormyresume.core.ui.component.ApplicationStatusSheet
import com.tailormyresume.core.ui.component.applicationStatusOptions
import com.tailormyresume.feature.tailor.impl.NoteLine
import com.tailormyresume.feature.tailor.impl.R
import com.tailormyresume.feature.tailor.impl.StatusCard
import com.tailormyresume.feature.tailor.impl.credits.CreditCounter
import com.tailormyresume.feature.tailor.impl.exportpreview.IconTile
import com.tailormyresume.feature.tailor.impl.jobLine

@Composable
internal fun ExportedScreen(
    uiState: ExportedUiState,
    actions: ExportedActions,
    modifier: Modifier = Modifier,
) {
    val toastState = rememberTmrToastState()
    val undoStatus = uiState.undoStatus
    val markedMessage = undoStatus?.let { markedMessage(uiState) }
    val undoLabel = stringResource(R.string.feature_tailor_impl_exported_undo)
    LaunchedEffect(undoStatus) {
        if (undoStatus == null || markedMessage == null) return@LaunchedEffect
        when (toastState.show(message = markedMessage, actionLabel = undoLabel)) {
            TmrToastResult.ActionPerformed -> actions.onUndoStatus()
            TmrToastResult.Dismissed -> actions.onDismissUndo()
        }
    }
    TmrScreen(
        modifier = modifier,
        sheet = false,
        header = {
            TmrInnerHeader(
                title = "",
                onBack = actions.onNavigateBack,
                backContentDescription = stringResource(
                    R.string.feature_tailor_impl_exported_navigation_back_description,
                ),
            )
        },
        bottomBar = { ExportedBottomBar(uiState, actions) },
        snackbarHost = { TmrToastHost(toastState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = TmrTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        ) {
            when (uiState.stage) {
                ExportedStage.LOADING -> ExportedLoading()
                ExportedStage.READY -> ExportedReady(uiState = uiState, actions = actions)
                ExportedStage.NO_APPLICATION,
                ExportedStage.NO_FILE,
                -> StatusCard(
                    kind = TmrSpotKind.Empty,
                    title = stringResource(R.string.feature_tailor_impl_exported_error_title),
                    body = stringResource(R.string.feature_tailor_impl_exported_error_body),
                )
            }
        }
    }
    if (uiState.statusSheetOpen) {
        ExportedStatusSheet(uiState = uiState, actions = actions)
    }
}

@Composable
private fun markedMessage(uiState: ExportedUiState): String {
    val label = uiState.status.label()
    val date = uiState.markedOn ?: return stringResource(R.string.feature_tailor_impl_exported_marked_message, label)
    return stringResource(R.string.feature_tailor_impl_exported_marked_message_dated, label, date)
}

@Composable
private fun ExportedBottomBar(uiState: ExportedUiState, actions: ExportedActions) {
    TmrBottomActionBar {
        if (uiState.stage == ExportedStage.READY) {
            TmrPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_exported_done),
                onClick = actions.onDone,
                modifier = Modifier.weight(1f),
                trailingIcon = TmrIcons.Check,
            )
        } else {
            TmrPrimaryButton(
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
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TmrLoadingWheel(contentDesc = loading)
        Text(text = loading, style = TmrTheme.typography.titleM, color = TmrTheme.colors.onSurface)
    }
}

@Composable
private fun ExportedReady(
    uiState: ExportedUiState,
    actions: ExportedActions,
) {
    ExportedHeadline()
    ExportedFileCard(uiState, actions)
    ExportedCreditCard(uiState)
    NoteLine(text = savedLine(uiState), icon = TmrIcons.Check)
    ExportedStatusCard(uiState, actions)
    ExportedNextSteps(actions)
}

@Composable
private fun ExportedHeadline() {
    val colors = TmrTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(colors.brand, TmrTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = TmrIcons.Check,
                contentDescription = null,
                tint = colors.onBrand,
                modifier = Modifier.size(TmrTheme.spacing.xxl),
            )
        }
        TmrHeadline(
            text = stringResource(R.string.feature_tailor_impl_exported_headline),
            style = TmrTheme.typography.headlineL,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun savedLine(uiState: ExportedUiState): String {
    val job = jobLine(uiState.jobTitle, uiState.jobCompany)
        ?: return stringResource(R.string.feature_tailor_impl_exported_saved_line_bare)
    return stringResource(R.string.feature_tailor_impl_exported_saved_line, job)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExportedFileCard(
    uiState: ExportedUiState,
    actions: ExportedActions,
) {
    TmrCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(icon = TmrIcons.Description)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.fileName.replace("_", "_\u200B"),
                    style = TmrTheme.typography.labelL,
                    color = TmrTheme.colors.onSurface,
                )
                if (uiState.canUseFile) {
                    Text(
                        text = fileDetail(uiState),
                        style = TmrTheme.typography.bodyS,
                        color = TmrTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
        if (uiState.canUseFile) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            ) {
                TmrSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_exported_share_action),
                    onClick = actions.onShare,
                    leadingIcon = TmrIcons.Share,
                    size = TmrButtonSize.Compact,
                )
                TmrSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_exported_open_action),
                    onClick = actions.onOpen,
                    leadingIcon = TmrIcons.OpenInNew,
                    size = TmrButtonSize.Compact,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.feature_tailor_impl_exported_share_missing),
                style = TmrTheme.typography.bodyM,
                color = TmrTheme.colors.body,
            )
        }
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
    val size = uiState.fileSizeBytes?.let { bytes ->
        stringResource(R.string.feature_tailor_impl_exported_file_size_kb, ((bytes + FILE_SIZE_ROUNDING) / BYTES_PER_KB).coerceAtLeast(1))
    }
    return listOfNotNull(format, pages, size).joinToString(" · ")
}

@Composable
private fun ExportedCreditCard(uiState: ExportedUiState) {
    if (!uiState.creditsKnown) return
    val left = uiState.creditsLeft
    TmrCard(modifier = Modifier.fillMaxWidth()) {
        CreditCounter(
            credits = left,
            previousCredits = uiState.creditsBefore,
            suffix = "",
            contentDescription = pluralStringResource(
                R.plurals.feature_tailor_impl_exported_credit_left_description,
                left,
                left,
            ),
            style = TmrTheme.typography.numeralHero,
        )
        Text(
            text = if (uiState.usesFreeCredit) {
                pluralStringResource(R.plurals.feature_tailor_impl_exported_credit_free_detail, left, left)
            } else {
                pluralStringResource(
                    if (uiState.creditsNeverExpire) {
                        R.plurals.feature_tailor_impl_exported_credit_paid_detail_never_expire
                    } else {
                        R.plurals.feature_tailor_impl_exported_credit_paid_detail
                    },
                    left,
                    left,
                    uiState.creditsBefore,
                )
            },
            style = TmrTheme.typography.titleS,
            color = TmrTheme.colors.onSurface,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExportedStatusCard(
    uiState: ExportedUiState,
    actions: ExportedActions,
) {
    TmrCard(modifier = Modifier.fillMaxWidth()) {
        if (uiState.asksForStatus) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_exported_status_question),
                style = TmrTheme.typography.titleM,
                color = TmrTheme.colors.onSurface,
            )
            TmrPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_exported_status_apply_action),
                onClick = actions.onOpenStatusSheet,
                leadingIcon = TmrIcons.Check,
                size = TmrButtonSize.Compact,
            )
            return@TmrCard
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            TmrApplicationStatusChip(
                kind = ApplicationStatusKindMapper().kindOf(uiState.status),
                label = uiState.status.label(),
            )
            uiState.markedOn?.let { date ->
                Text(
                    text = stringResource(R.string.feature_tailor_impl_exported_marked_on, date),
                    style = TmrTheme.typography.titleS,
                    color = TmrTheme.colors.onSurfaceVariant,
                )
            }
        }
        TmrTextButton(
            label = stringResource(R.string.feature_tailor_impl_exported_status_change_action),
            onClick = actions.onOpenStatusSheet,
        )
    }
}

@Composable
private fun ExportedNextSteps(actions: ExportedActions) {
    ExportedActionRow(
        icon = TmrIcons.Description,
        title = stringResource(R.string.feature_tailor_impl_exported_next_prep_questions),
        onClick = actions.onGetPrepQuestions,
    )
    ExportedActionRow(
        icon = TmrIcons.Edit,
        title = stringResource(R.string.feature_tailor_impl_exported_next_cover_letter),
        onClick = actions.onWriteCoverLetter,
    )
}

@Composable
private fun ExportedActionRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TmrTheme.colors.card, TmrTheme.shapes.field)
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = TmrTheme.spacing.d64)
            .padding(start = TmrTheme.spacing.d12 - TmrTheme.spacing.d2, end = TmrTheme.spacing.lg, top = TmrTheme.spacing.d8, bottom = TmrTheme.spacing.d8),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.d12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon = icon)
        Text(
            text = title,
            style = TmrTheme.typography.titleS,
            color = TmrTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
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
        title = stringResource(R.string.feature_tailor_impl_exported_status_sheet_title),
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

private const val BYTES_PER_KB = 1024

private const val FILE_SIZE_ROUNDING = 512
