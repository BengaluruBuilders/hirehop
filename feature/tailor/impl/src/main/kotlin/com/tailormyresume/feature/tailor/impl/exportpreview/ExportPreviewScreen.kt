package com.tailormyresume.feature.tailor.impl.exportpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrPaperColors
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrStepProgress
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.feature.tailor.impl.R

@Composable
internal fun ExportPreviewScreen(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
    modifier: Modifier = Modifier,
) {
    TmrScreen(
        modifier = modifier,
        sheet = false,
        header = {
            TmrInnerHeader(
                title = stringResource(R.string.feature_tailor_impl_export_preview_title),
                onBack = actions.onNavigateBack,
                backContentDescription = stringResource(
                    R.string.feature_tailor_impl_export_preview_navigation_back_description,
                ),
            )
        },
        bottomBar = exportPreviewBottomBar(uiState = uiState, actions = actions),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = TmrTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
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
        { ExportPreviewDownloadBar(uiState = uiState, actions = actions, enabled = !uiState.isOffline) }
    }

    ExportPreviewStage.RENDERING -> {
        { ExportPreviewDownloadBar(uiState = uiState, actions = actions, enabled = false) }
    }

    ExportPreviewStage.EXPORTING -> {
        { ExportingSheet(uiState = uiState, onCancel = actions.onCancel) }
    }

    ExportPreviewStage.PREVIEW_FAILED,
    ExportPreviewStage.EXPORT_FAILED,
    -> {
        { ExportPreviewRetryBar(onRetry = actions.onRetry) }
    }

    ExportPreviewStage.NO_DOCUMENT -> null
}

@Composable
private fun ExportPreviewBody(uiState: ExportPreviewUiState, actions: ExportPreviewActions) {
    if (uiState.stage == ExportPreviewStage.NO_DOCUMENT) {
        TmrCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_export_preview_empty_title),
                style = TmrTheme.typography.titleM,
                color = TmrTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_export_preview_empty_body),
                style = TmrTheme.typography.bodyM,
                color = TmrTheme.colors.body,
            )
        }
        return
    }
    val banner = exportPreviewBanner(uiState)
    if (banner != null) NoticeBanner(message = banner.first, tone = banner.second)
    ExportPreviewPage(uiState = uiState, compact = banner != null)
    ExportPreviewFileDetails(uiState = uiState, actions = actions)
}

@Composable
private fun exportPreviewBanner(uiState: ExportPreviewUiState): Pair<String, NoticeTone>? = when {
    uiState.stage == ExportPreviewStage.PREVIEW_FAILED || uiState.stage == ExportPreviewStage.EXPORT_FAILED ->
        stringResource(R.string.feature_tailor_impl_export_preview_error_body) to NoticeTone.Error

    uiState.stage != ExportPreviewStage.PREVIEW_READY && uiState.stage != ExportPreviewStage.EXPORTING -> null

    uiState.isOffline ->
        stringResource(R.string.feature_tailor_impl_export_preview_offline_banner) to NoticeTone.Offline

    uiState.isFreeBeta ->
        stringResource(R.string.feature_tailor_impl_export_preview_credit_beta) to NoticeTone.Good

    uiState.needsCredits ->
        stringResource(R.string.feature_tailor_impl_export_preview_credit_none) to NoticeTone.Quiet

    else -> null
}

@Composable
private fun ExportPreviewPage(uiState: ExportPreviewUiState, compact: Boolean) {
    val sheet = uiState.sheet
    val paperWidth = if (compact) PAPER_WIDTH_COMPACT else PAPER_WIDTH
    val widthModifier = Modifier.widthIn(max = paperWidth).fillMaxWidth()
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        val paperScroll = rememberScrollState()
        when {
            sheet != null -> Box(
                modifier = widthModifier
                    .aspectRatio(PAPER_ASPECT)
                    .background(TmrPaperColors.Page, RoundedCornerShape(TmrTheme.spacing.d4 + TmrTheme.spacing.d2))
                    .padding(TmrTheme.spacing.d20),
            ) {
                ExportPaper(sheet = sheet, scrollState = paperScroll)
            }

            uiState.stage == ExportPreviewStage.RENDERING -> PagePlaceholder(modifier = widthModifier) {
                RenderingSteps()
            }

            else -> PagePlaceholder(modifier = widthModifier) {
                Icon(
                    imageVector = TmrIcons.Description,
                    contentDescription = null,
                    tint = TmrTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(TmrTheme.spacing.xxxl),
                )
                Text(
                    text = stringResource(R.string.feature_tailor_impl_export_preview_failed_placeholder),
                    style = TmrTheme.typography.labelL,
                    color = TmrTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text(
            text = pageCaption(uiState),
            style = TmrTheme.typography.labelM,
            color = TmrTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun pageCaption(uiState: ExportPreviewUiState): String = when {
    uiState.sheet == null && uiState.stage == ExportPreviewStage.RENDERING ->
        stringResource(R.string.feature_tailor_impl_export_preview_rendering_caption)

    uiState.sheet == null -> stringResource(R.string.feature_tailor_impl_export_preview_no_preview)

    else -> stringResource(R.string.feature_tailor_impl_export_preview_paper_meta, uiState.format.label())
}

@Composable
private fun PagePlaceholder(modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .heightIn(min = TmrTheme.spacing.d64 * PLACEHOLDER_HEIGHT_UNITS)
            .background(TmrTheme.colors.card, RoundedCornerShape(TmrTheme.spacing.d4 + TmrTheme.spacing.d2))
            .border(
                TmrTheme.spacing.d2,
                TmrTheme.colors.outlineVariant,
                RoundedCornerShape(TmrTheme.spacing.d4 + TmrTheme.spacing.d2),
            )
            .padding(TmrTheme.spacing.d20),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md, Alignment.CenterVertically),
    ) {
        content()
    }
}

@Composable
private fun RenderingSteps() {
    TmrStepProgress(
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

@Composable
private fun ExportPreviewFileDetails(uiState: ExportPreviewUiState, actions: ExportPreviewActions) {
    ExportOptionRow(
        options = ExportFormat.entries,
        selected = uiState.format,
        labelOf = { format -> format.label() },
        onSelect = actions.onSelectFormat,
        groupDescription = stringResource(R.string.feature_tailor_impl_export_preview_format_group),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TmrTheme.colors.card, TmrTheme.shapes.field)
            .defaultMinSize(minHeight = TmrTheme.spacing.d64)
            .padding(horizontal = TmrTheme.spacing.d12 + TmrTheme.spacing.xxs, vertical = TmrTheme.spacing.d12),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.d12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = TmrIcons.Description,
            contentDescription = null,
            tint = TmrTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(TmrTheme.spacing.xxl),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_export_preview_file_label),
                style = TmrTheme.typography.labelM,
                color = TmrTheme.colors.onSurfaceVariant,
            )
            Text(text = uiState.fileName, style = TmrTheme.typography.labelL, color = TmrTheme.colors.onSurface)
        }
    }
}

@Composable
private fun ExportPreviewDownloadBar(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
    enabled: Boolean,
) {
    val creditLine = exportPreviewCreditLine(uiState)
    TmrBottomActionBar(
        creditDisclosure = if (creditLine.isEmpty()) {
            null
        } else {
            {
                Text(
                    text = creditLine,
                    style = TmrTheme.typography.bodyS,
                    color = TmrTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
    ) {
        TmrPrimaryButton(
            label = stringResource(
                when (uiState.format) {
                    ExportFormat.PDF -> R.string.feature_tailor_impl_export_preview_download_pdf
                    ExportFormat.DOCX -> R.string.feature_tailor_impl_export_preview_download_docx
                },
            ),
            onClick = if (uiState.needsCredits) actions.onBuyCredits else actions.onExport,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            trailingIcon = TmrIcons.Download,
        )
    }
}

@Composable
private fun ExportPreviewRetryBar(onRetry: () -> Unit) {
    TmrBottomActionBar {
        TmrPrimaryButton(
            label = stringResource(R.string.feature_tailor_impl_export_preview_error_action),
            onClick = onRetry,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ExportingSheet(uiState: ExportPreviewUiState, onCancel: () -> Unit) {
    val colors = TmrTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.sheet, TmrTheme.shapes.modalSheet)
            .navigationBarsPadding()
            .padding(
                start = TmrTheme.spacing.gutter,
                end = TmrTheme.spacing.gutter,
                top = TmrTheme.spacing.d12,
                bottom = TmrTheme.spacing.gutter,
            ),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.d12),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = TmrTheme.spacing.d32 + TmrTheme.spacing.d4, height = TmrTheme.spacing.d4)
                .background(colors.outlineVariant, TmrTheme.shapes.pill),
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_exporting_title),
            style = TmrTheme.typography.headlineM,
            color = colors.onSurface,
        )
        TmrStepProgress(
            modifier = Modifier.fillMaxWidth(),
            stepNames = listOf(
                stringResource(R.string.feature_tailor_impl_export_preview_step_making, uiState.format.label()),
                stringResource(R.string.feature_tailor_impl_export_preview_step_saving),
            ),
            currentStepIndex = 0,
            ordinalLabel = "",
            stepStatuses = listOf(
                stringResource(R.string.feature_tailor_impl_export_preview_step_status_active),
                stringResource(R.string.feature_tailor_impl_export_preview_step_status_waiting),
            ),
            footnote = stringResource(R.string.feature_tailor_impl_export_preview_exporting_footnote),
        )
        if (!uiState.isSpending) {
            TmrSecondaryButton(
                label = stringResource(R.string.feature_tailor_impl_export_preview_cancel),
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun exportPreviewCreditLine(uiState: ExportPreviewUiState): String = when {
    uiState.isOffline || uiState.isFreeBeta || uiState.alreadyUnlocked || !uiState.creditsKnown || uiState.needsCredits -> ""
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

private val PAPER_WIDTH = 233.dp
private val PAPER_WIDTH_COMPACT = 185.dp
private const val PAPER_ASPECT = 233f / 330f
private const val PLACEHOLDER_HEIGHT_UNITS = 4f
