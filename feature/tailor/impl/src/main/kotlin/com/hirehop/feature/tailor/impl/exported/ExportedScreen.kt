package com.hirehop.feature.tailor.impl.exported

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhApplicationStatusChip
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.ui.ApplicationStatusKindMapper
import com.hirehop.core.ui.component.ApplicationStatusSheet
import com.hirehop.core.ui.component.applicationStatusOptions
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.exportpreview.ExportFormat

private val PAGE_SURFACE = Color(0xFFFFFFFF)

@Composable
internal fun ExportedScreen(
    uiState: ExportedUiState,
    actions: ExportedActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_tailor_impl_exported_title),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                navigationIconContentDescription = stringResource(
                    R.string.feature_tailor_impl_exported_navigation_back_description,
                ),
                onNavigationClick = actions.onNavigateBack,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState.stage) {
                ExportedStage.IDLE -> ExportedLoading()
                ExportedStage.READY -> ExportedBody(uiState = uiState, actions = actions)
                ExportedStage.NO_APPLICATION,
                ExportedStage.NO_FILE,
                -> ExportedMissing(actions = actions)
            }
        }
    }
    if (uiState.statusSheetOpen) {
        val statusLabels = ApplicationStatus.entries.associateWith { status -> applicationStatusLabel(status) }
        ApplicationStatusSheet(
            current = uiState.status,
            options = applicationStatusOptions { status -> statusLabels.getValue(status) },
            onConfirm = actions.onConfirmStatus,
            onDismiss = actions.onDismissStatusSheet,
            saveLabel = stringResource(R.string.feature_tailor_impl_exported_status_sheet_save),
            cancelLabel = stringResource(R.string.feature_tailor_impl_exported_status_sheet_cancel),
            eyebrow = uiState.jobLine(),
            title = stringResource(R.string.feature_tailor_impl_exported_status_sheet_title),
            note = stringResource(R.string.feature_tailor_impl_exported_status_sheet_note),
        )
    }
}

@Composable
private fun ExportedBody(
    uiState: ExportedUiState,
    actions: ExportedActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d16, vertical = HhTheme.spacing.d16),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        if (uiState.hasJob) {
            Text(
                text = uiState.jobLine(),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        ExportedFileCard(uiState = uiState, onShare = actions.onShare)
        ExportedSavedLine(uiState = uiState)
        if (uiState.asksForStatus) {
            ExportedStatusAsk(onOpenStatusSheet = actions.onOpenStatusSheet)
        } else {
            ExportedStatusSet(uiState = uiState, onOpenStatusSheet = actions.onOpenStatusSheet)
        }
        if (uiState.creditsKnown) {
            ExportedCreditBlock(uiState = uiState)
        }
        ExportedNextBlock()
        ExportedReferralRow()
    }
}

@Composable
private fun ExportedFileCard(
    uiState: ExportedUiState,
    onShare: () -> Unit,
) {
    HhCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            ExportedPageThumbnail(
                description = stringResource(
                    R.string.feature_tailor_impl_exported_file_name_description,
                    uiState.fileName,
                ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
            ) {
                Text(
                    text = uiState.fileName,
                    style = HhTheme.typography.monoSmall,
                    color = HhTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.feature_tailor_impl_exported_file_facts,
                        uiState.format.label(),
                    ),
                    style = HhTheme.typography.bodySmall,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
        HhButton(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
            enabled = uiState.canShare,
            text = {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_exported_share_action),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
        if (!uiState.canShare) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_exported_share_missing),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        if (uiState.shareState == ExportedShareState.REQUESTED) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_exported_share_requested),
                style = HhTheme.typography.titleSmall,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_exported_share_requested_note),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExportedPageThumbnail(description: String) {
    Column(
        modifier = Modifier
            .width(SPACING_FORTY_FOUR)
            .height(SPACING_FIFTY_EIGHT)
            .background(color = PAGE_SURFACE, shape = RoundedCornerShape(HhTheme.shapes.xs))
            .border(
                width = 1.dp,
                color = HhTheme.colors.hairline,
                shape = RoundedCornerShape(HhTheme.shapes.xs),
            )
            .padding(HhTheme.spacing.d8)
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d4),
    ) {
        repeat(PAGE_THUMBNAIL_LINES) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (index == 0) 0.6f else 1f)
                    .height(2.dp)
                    .background(
                        color = HhTheme.colors.onSurfaceVariant.copy(alpha = PAGE_THUMBNAIL_INK_ALPHA),
                        shape = RoundedCornerShape(HhTheme.shapes.full),
                    ),
            )
        }
    }
}

@Composable
private fun ExportedSavedLine(uiState: ExportedUiState) {
    val description = stringResource(R.string.feature_tailor_impl_exported_saved_line_description)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(HhTheme.spacing.d20)
                .border(
                    width = 1.5.dp,
                    color = HhTheme.colors.onSurface,
                    shape = RoundedCornerShape(HhTheme.shapes.full),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = HhIcons.Check,
                contentDescription = null,
                tint = HhTheme.colors.onSurface,
                modifier = Modifier.size(HhTheme.spacing.d12),
            )
        }
        Text(
            text = stringResource(
                R.string.feature_tailor_impl_exported_saved_line,
                uiState.jobTitle,
                uiState.jobCompany,
            ),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun ExportedStatusAsk(onOpenStatusSheet: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = HhTheme.colors.surface,
                shape = RoundedCornerShape(HhTheme.shapes.md),
            )
            .border(
                width = 1.dp,
                color = HhTheme.colors.hairline,
                shape = RoundedCornerShape(HhTheme.shapes.md),
            )
            .padding(
                start = SPACING_FOURTEEN,
                end = HhTheme.spacing.d8,
                top = HhTheme.spacing.sm,
                bottom = HhTheme.spacing.sm,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_exported_status_question),
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        HhOutlinedButton(
            onClick = onOpenStatusSheet,
            modifier = Modifier.heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_exported_status_apply_action),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}

@Composable
private fun ExportedStatusSet(
    uiState: ExportedUiState,
    onOpenStatusSheet: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (uiState.statusJustSet) {
                    HhTheme.colors.primaryContainer
                } else {
                    HhTheme.colors.surface
                },
                shape = RoundedCornerShape(HhTheme.shapes.md),
            )
            .border(
                width = 1.dp,
                color = HhTheme.colors.hairline,
                shape = RoundedCornerShape(HhTheme.shapes.md),
            )
            .padding(
                start = SPACING_FOURTEEN,
                end = HhTheme.spacing.d4,
                top = HhTheme.spacing.sm,
                bottom = HhTheme.spacing.sm,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_exported_status_label),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhApplicationStatusChip(
            kind = ApplicationStatusKindMapper().kindOf(uiState.status),
            label = applicationStatusLabel(uiState.status),
        )
        Box(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.feature_tailor_impl_exported_status_change_action),
            style = HhTheme.typography.labelLarge,
            color = HhTheme.colors.primary,
            modifier = Modifier
                .defaultMinSize(minHeight = HhTheme.spacing.d48)
                .clickable(onClick = onOpenStatusSheet)
                .padding(horizontal = HhTheme.spacing.sm, vertical = HhTheme.spacing.sm),
        )
    }
}

@Composable
private fun ExportedCreditBlock(uiState: ExportedUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = HhTheme.spacing.d8),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        HhDivider()
        if (uiState.usesFreeCredit) {
            ExportedFreeCreditLines(uiState = uiState)
        } else {
            ExportedPaidCreditRow(uiState = uiState)
        }
        HhDivider()
    }
}

@Composable
private fun ExportedFreeCreditLines(uiState: ExportedUiState) {
    Text(
        text = stringResource(R.string.feature_tailor_impl_exported_credit_free_title),
        style = HhTheme.typography.titleMedium,
        color = HhTheme.colors.onSurface,
    )
    Text(
        text = stringResource(R.string.feature_tailor_impl_exported_credit_free_note),
        style = HhTheme.typography.bodySmall,
        color = HhTheme.colors.onSurfaceVariant,
    )
    Text(
        text = pluralStringResource(
            R.plurals.feature_tailor_impl_exported_credit_free_left,
            uiState.creditsLeft,
            uiState.creditsLeft,
        ),
        style = HhTheme.typography.bodySmall,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun ExportedPaidCreditRow(uiState: ExportedUiState) {
    val description = pluralStringResource(
        R.plurals.feature_tailor_impl_exported_credit_left_description,
        uiState.creditsLeft,
        uiState.creditsLeft,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = HhTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.Bottom,
    ) {
        ExportedCreditNumeral(
            credits = uiState.creditsLeft,
            previousCredits = uiState.creditsBefore,
            contentDescription = description,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = HhTheme.spacing.d2),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d4),
        ) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_exported_credit_left_caption),
                style = HhTheme.typography.titleLarge,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(
                    if (uiState.creditsNeverExpire) {
                        R.string.feature_tailor_impl_exported_credit_paid_note
                    } else {
                        R.string.feature_tailor_impl_exported_credit_paid_note_unknown_expiry
                    },
                ),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExportedNextBlock() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_exported_next_eyebrow),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        ExportedLaterReleaseRow(
            label = stringResource(R.string.feature_tailor_impl_exported_next_prep_questions),
        )
        HhDivider()
        ExportedLaterReleaseRow(
            label = stringResource(R.string.feature_tailor_impl_exported_next_cover_letter),
        )
    }
}

@Composable
private fun ExportedLaterReleaseRow(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HhTheme.spacing.d48),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = label,
            style = HhTheme.typography.labelLarge,
            color = HhTheme.colors.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_exported_next_later_release),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExportedReferralRow() {
    Column(modifier = Modifier.fillMaxWidth()) {
        HhDivider()
        ExportedReferralLine()
    }
}

@Composable
private fun ExportedReferralLine() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = HhTheme.spacing.d8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_exported_referral_note),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_exported_referral_action),
            style = HhTheme.typography.labelLarge,
            color = HhTheme.colors.onSurfaceVariant,
            modifier = Modifier.heightIn(min = HhTheme.spacing.d48),
        )
    }
}

@Composable
private fun ExportedLoading() {
    val loading = stringResource(R.string.feature_tailor_impl_exported_loading)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhLoadingWheel(contentDesc = loading)
        Text(
            text = loading,
            style = HhTheme.typography.titleLarge,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun ExportedMissing(actions: ExportedActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        Text(
            text = stringResource(R.string.feature_tailor_impl_exported_error_heading),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_exported_error_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhButton(
            onClick = actions.onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_exported_error_back),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}

@Composable
private fun ExportFormat.label(): String = stringResource(
    when (this) {
        ExportFormat.PDF -> R.string.feature_tailor_impl_exported_format_pdf
        ExportFormat.DOCX -> R.string.feature_tailor_impl_exported_format_docx
    },
)

@Composable
private fun applicationStatusLabel(status: ApplicationStatus): String = stringResource(
    when (status) {
        ApplicationStatus.SAVED -> R.string.feature_tailor_impl_exported_status_saved
        ApplicationStatus.APPLIED -> R.string.feature_tailor_impl_exported_status_applied
        ApplicationStatus.INTERVIEW -> R.string.feature_tailor_impl_exported_status_interview
        ApplicationStatus.OFFER -> R.string.feature_tailor_impl_exported_status_offer
        ApplicationStatus.REJECTED -> R.string.feature_tailor_impl_exported_status_rejected
        ApplicationStatus.NO_RESPONSE -> R.string.feature_tailor_impl_exported_status_no_response
    },
)

private fun ExportedUiState.jobLine(): String = listOf(jobTitle, jobCompany)
    .filter { part -> part.isNotBlank() }
    .joinToString(SEPARATOR)

private val SPACING_FOURTEEN = 14.dp

private val SPACING_FORTY_FOUR = 44.dp

private val SPACING_FIFTY_EIGHT = 58.dp

private const val SEPARATOR = " · "

private const val PAGE_THUMBNAIL_LINES = 5

private const val PAGE_THUMBNAIL_INK_ALPHA = 0.32f
