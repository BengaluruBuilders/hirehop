package com.hirehop.feature.tailor.impl.exported

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
import com.hirehop.core.designsystem.component.HhApplicationStatusChip
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButtonSize
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSecondaryButton
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
import com.hirehop.feature.tailor.impl.NoteLine
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.StatusCard
import com.hirehop.feature.tailor.impl.credits.CreditCounter
import com.hirehop.feature.tailor.impl.exportpreview.IconTile
import com.hirehop.feature.tailor.impl.jobLine

@Composable
internal fun ExportedScreen(
    uiState: ExportedUiState,
    actions: ExportedActions,
    modifier: Modifier = Modifier,
) {
    val toastState = rememberHhToastState()
    val undoStatus = uiState.undoStatus
    val markedMessage = undoStatus?.let { markedMessage(uiState) }
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
                title = "",
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
                -> StatusCard(
                    kind = HhSpotKind.Empty,
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
private fun ExportedReady(
    uiState: ExportedUiState,
    actions: ExportedActions,
) {
    ExportedHeadline()
    ExportedFileCard(uiState, actions)
    ExportedCreditCard(uiState)
    NoteLine(text = savedLine(uiState), icon = HhIcons.Check)
    ExportedStatusCard(uiState, actions)
    ExportedNextSteps(actions)
}

@Composable
private fun ExportedHeadline() {
    val colors = HhTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(colors.brand, HhTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = HhIcons.Check,
                contentDescription = null,
                tint = colors.onBrand,
                modifier = Modifier.size(HhTheme.spacing.xxl),
            )
        }
        HhHeadline(
            text = stringResource(R.string.feature_tailor_impl_exported_headline),
            style = HhTheme.typography.headlineL,
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
    HhCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(icon = HhIcons.Description)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.fileName,
                    style = HhTheme.typography.labelL,
                    color = HhTheme.colors.onSurface,
                )
                if (uiState.canUseFile) {
                    Text(
                        text = fileDetail(uiState),
                        style = HhTheme.typography.bodyS,
                        color = HhTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
        if (uiState.canUseFile) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                HhSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_exported_share_action),
                    onClick = actions.onShare,
                    leadingIcon = HhIcons.Share,
                    size = HhButtonSize.Compact,
                )
                HhSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_exported_open_action),
                    onClick = actions.onOpen,
                    leadingIcon = HhIcons.OpenInNew,
                    size = HhButtonSize.Compact,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.feature_tailor_impl_exported_share_missing),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
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
    return listOfNotNull(format, pages, uiState.templateName).joinToString(" · ")
}

@Composable
private fun ExportedCreditCard(uiState: ExportedUiState) {
    if (!uiState.creditsKnown) return
    val left = uiState.creditsLeft
    HhCard(modifier = Modifier.fillMaxWidth()) {
        CreditCounter(
            credits = left,
            previousCredits = uiState.creditsBefore,
            suffix = "",
            contentDescription = pluralStringResource(
                R.plurals.feature_tailor_impl_exported_credit_left_description,
                left,
                left,
            ),
            style = HhTheme.typography.numeralHero,
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
            style = HhTheme.typography.titleS,
            color = HhTheme.colors.onSurface,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExportedStatusCard(
    uiState: ExportedUiState,
    actions: ExportedActions,
) {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        if (uiState.asksForStatus) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_exported_status_question),
                style = HhTheme.typography.titleM,
                color = HhTheme.colors.onSurface,
            )
            HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_exported_status_apply_action),
                onClick = actions.onOpenStatusSheet,
                leadingIcon = HhIcons.Check,
                size = HhButtonSize.Compact,
            )
            return@HhCard
        }
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
                    style = HhTheme.typography.titleS,
                    color = HhTheme.colors.onSurfaceVariant,
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
    ExportedActionRow(
        icon = HhIcons.Description,
        title = stringResource(R.string.feature_tailor_impl_exported_next_prep_questions),
        onClick = actions.onGetPrepQuestions,
    )
    ExportedActionRow(
        icon = HhIcons.Edit,
        title = stringResource(R.string.feature_tailor_impl_exported_next_cover_letter),
        onClick = actions.onWriteCoverLetter,
    )
}

@Composable
private fun ExportedActionRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HhTheme.colors.card, HhTheme.shapes.field)
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = HhTheme.spacing.d64)
            .padding(start = HhTheme.spacing.d12 - HhTheme.spacing.d2, end = HhTheme.spacing.lg, top = HhTheme.spacing.d8, bottom = HhTheme.spacing.d8),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon = icon)
        Text(
            text = title,
            style = HhTheme.typography.titleS,
            color = HhTheme.colors.onSurface,
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
