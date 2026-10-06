package com.hirehop.feature.tailor.impl.exportpreview

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.component.HhAccent
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhExportPreviewFrame
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSolidCard
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ExportFormat
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.jobLine

@Composable
internal fun ExportPreviewScreen(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
                title = stringResource(R.string.feature_tailor_impl_export_preview_title),
                subtitle = jobLine(uiState.jobTitle, uiState.jobCompany),
                onBack = actions.onNavigateBack,
                backContentDescription = stringResource(
                    R.string.feature_tailor_impl_export_preview_navigation_back_description,
                ),
            )
        },
        bottomBar = exportPreviewBottomBar(uiState = uiState, actions = actions),
        bottomBarNotice = exportPreviewBottomNotice(uiState),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = HhTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            ExportPreviewBody(uiState = uiState, actions = actions)
        }
    }
}

@Composable
private fun exportPreviewBottomBar(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
): (@Composable () -> Unit)? = when (uiState.stage) {
    ExportPreviewStage.PREVIEW_READY -> {
        { ExportPreviewDownloadBar(uiState = uiState, actions = actions) }
    }

    ExportPreviewStage.PREVIEW_FAILED,
    ExportPreviewStage.EXPORT_FAILED,
    -> {
        { ExportPreviewRetryBar(onRetry = actions.onRetry) }
    }

    else -> null
}

@Composable
private fun exportPreviewBottomNotice(uiState: ExportPreviewUiState): (@Composable () -> Unit)? {
    if (uiState.stage != ExportPreviewStage.PREVIEW_READY) return null
    val creditLine = exportPreviewCreditLine(uiState)
    return if (creditLine.isEmpty()) {
        null
    } else {
        ({ ExportPreviewCreditDisclosure(uiState = uiState, line = creditLine) })
    }
}

@Composable
private fun ExportPreviewBody(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
) {
    when (uiState.stage) {
        ExportPreviewStage.RENDERING -> HhCard(modifier = Modifier.fillMaxWidth()) {
            HhStepProgress(
                modifier = Modifier.fillMaxWidth(),
                stepNames = listOf(
                    stringResource(R.string.feature_tailor_impl_export_preview_step_setting),
                    stringResource(R.string.feature_tailor_impl_export_preview_step_checking),
                ),
                currentStepIndex = 0,
                ordinalLabel = stringResource(R.string.feature_tailor_impl_export_preview_step_eyebrow),
                stepDetails = listOf(stringResource(R.string.feature_tailor_impl_export_preview_step_setting_detail), null),
                footnote = stringResource(R.string.feature_tailor_impl_export_preview_rendering_footnote),
            )
        }

        ExportPreviewStage.EXPORTING -> HhCard(modifier = Modifier.fillMaxWidth()) {
            HhStepProgress(
                modifier = Modifier.fillMaxWidth(),
                stepNames = listOf(
                    stringResource(R.string.feature_tailor_impl_export_preview_step_setting),
                    stringResource(R.string.feature_tailor_impl_export_preview_step_making, uiState.format.label()),
                    stringResource(R.string.feature_tailor_impl_export_preview_step_saving),
                ),
                currentStepIndex = 1,
                ordinalLabel = stringResource(
                    if (uiState.isFreeBeta) {
                        R.string.feature_tailor_impl_export_preview_exporting_eyebrow
                    } else {
                        R.string.feature_tailor_impl_export_preview_exporting_eyebrow_credit
                    },
                ),
                footnote = stringResource(R.string.feature_tailor_impl_export_preview_exporting_footnote),
            )
        }

        ExportPreviewStage.PREVIEW_READY -> ExportPreviewReady(uiState = uiState, actions = actions)

        ExportPreviewStage.PREVIEW_FAILED,
        ExportPreviewStage.EXPORT_FAILED,
        -> {
            val title = stringResource(R.string.feature_tailor_impl_export_preview_error_title)
            HhSolidCard(
                accent = HhAccent.Coral,
                monogram = "!",
                title = title,
                subtitle = stringResource(R.string.feature_tailor_impl_export_preview_error_body),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ExportPreviewStage.NO_DOCUMENT -> {
            val title = stringResource(R.string.feature_tailor_impl_export_preview_empty_title)
            HhSolidCard(
                accent = HhAccent.Jade,
                monogram = "i",
                title = title,
                subtitle = stringResource(R.string.feature_tailor_impl_export_preview_empty_body),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ExportPreviewReady(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
) {
    HhOfflineBanner(
        message = stringResource(R.string.feature_tailor_impl_export_preview_offline_banner),
        visible = uiState.isOffline,
    )
    val sheet = uiState.sheet
    if (sheet != null) {
        val paperScroll = rememberScrollState()
        HhExportPreviewFrame(
            meta = stringResource(
                R.string.feature_tailor_impl_export_preview_paper_meta,
                uiState.format.label(),
            ),
            caption = stringResource(
                if (paperScroll.canScrollForward) {
                    R.string.feature_tailor_impl_export_preview_paper_caption_scroll
                } else {
                    R.string.feature_tailor_impl_export_preview_paper_caption_end
                },
            ),
        ) {
            ExportPaper(sheet = sheet, scrollState = paperScroll)
        }
    }
    HhCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(HhTheme.spacing.d16 + HhTheme.spacing.d12),
    ) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_file_label),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = uiState.fileName,
            style = HhTheme.typography.factId,
            color = HhTheme.colors.onSurface,
        )
        ExportOptionRow(
            options = ExportFormat.entries,
            selected = uiState.format,
            labelOf = { format -> format.label() },
            onSelect = actions.onSelectFormat,
            groupDescription = stringResource(R.string.feature_tailor_impl_export_preview_format_group),
        )
        ExportPreviewAtsNote()
    }
}

@Composable
private fun ExportPreviewAtsNote() {
    val icon = @Composable {
        Icon(
            imageVector = HhIcons.Check,
            contentDescription = null,
            tint = HhTheme.colors.primary,
            modifier = Modifier.size(HhTheme.spacing.lg),
        )
    }
    val text = @Composable {
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_ats_note),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurface,
        )
    }
    if (LocalDensity.current.fontScale >= LARGE_FONT_SCALE) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
            icon()
            text()
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            text()
        }
    }
}

@Composable
private fun ExportPreviewDownloadBar(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
) {
    HhBottomActionBar {
        HhPrimaryButton(
            label = stringResource(
                when (uiState.format) {
                    ExportFormat.PDF -> R.string.feature_tailor_impl_export_preview_download_pdf
                    ExportFormat.DOCX -> R.string.feature_tailor_impl_export_preview_download_docx
                },
            ),
            onClick = if (uiState.needsCredits) actions.onBuyCredits else actions.onExport,
            modifier = Modifier.weight(1f),
            enabled = !uiState.isOffline,
            trailingIcon = HhIcons.Download,
        )
    }
}

@Composable
private fun ExportPreviewRetryBar(onRetry: () -> Unit) {
    HhBottomActionBar {
        HhPrimaryButton(
            label = stringResource(R.string.feature_tailor_impl_export_preview_error_action),
            onClick = onRetry,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ExportPreviewCreditDisclosure(uiState: ExportPreviewUiState, line: String) {
    val colors = HhTheme.colors
    val fill = when {
        uiState.needsCredits -> colors.special
        uiState.isOffline -> colors.neutralContainer
        else -> colors.primaryContainer
    }
    val content = when {
        uiState.needsCredits -> colors.onSpecial
        uiState.isOffline -> colors.onNeutralContainer
        else -> colors.onPrimaryContainer
    }
    val icon = when {
        uiState.needsCredits -> HhIcons.Info
        uiState.isOffline -> HhIcons.Info
        else -> HhIcons.Download
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HhTheme.shapes.pillRow)
            .background(fill)
            .defaultMinSize(minHeight = HhTheme.spacing.touch)
            .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.sm),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(HhTheme.spacing.lg),
            )
            Text(
                text = line,
                style = HhTheme.typography.bodyM,
                color = content,
            )
        }
    }
}

@Composable
private fun exportPreviewCreditLine(uiState: ExportPreviewUiState): String = when {
    uiState.isOffline -> stringResource(R.string.feature_tailor_impl_export_preview_credit_offline)
    uiState.isFreeBeta -> stringResource(R.string.feature_tailor_impl_export_preview_credit_beta)
    !uiState.creditsKnown -> ""
    uiState.needsCredits -> stringResource(R.string.feature_tailor_impl_export_preview_credit_none)
    uiState.purchasedCredits == 0 -> pluralStringResource(
        R.plurals.feature_tailor_impl_export_preview_credit_free,
        uiState.freeCredits,
        uiState.freeCredits,
    )

    uiState.freeCredits == 0 -> pluralStringResource(
        R.plurals.feature_tailor_impl_export_preview_credit_paid,
        uiState.purchasedCredits,
        uiState.purchasedCredits,
    )

    else -> pluralStringResource(
        R.plurals.feature_tailor_impl_export_preview_credit_both,
        uiState.freeCredits,
        uiState.freeCredits,
        uiState.purchasedCredits,
    )
}

@Composable
private fun ExportFormat.label(): String = stringResource(
    when (this) {
        ExportFormat.PDF -> R.string.feature_tailor_impl_export_preview_format_pdf
        ExportFormat.DOCX -> R.string.feature_tailor_impl_export_preview_format_docx
    },
)

private const val LARGE_FONT_SCALE = 1.5f
