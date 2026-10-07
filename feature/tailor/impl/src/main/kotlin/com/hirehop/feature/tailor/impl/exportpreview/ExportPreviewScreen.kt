package com.hirehop.feature.tailor.impl.exportpreview

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
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhPaperColors
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ExportFormat
import com.hirehop.feature.tailor.impl.NoteLine
import com.hirehop.feature.tailor.impl.R

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
        { ExportPreviewDownloadBar(uiState = uiState, actions = actions, enabled = !uiState.isOffline) }
    }

    ExportPreviewStage.RENDERING -> {
        { ExportPreviewDownloadBar(uiState = uiState, actions = actions, enabled = false) }
    }

    ExportPreviewStage.EXPORTING -> {
        { ExportingSheet(uiState = uiState) }
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
        HhCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_export_preview_empty_title),
                style = HhTheme.typography.titleM,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_export_preview_empty_body),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
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
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        val paperScroll = rememberScrollState()
        when {
            sheet != null -> Box(
                modifier = widthModifier
                    .aspectRatio(PAPER_ASPECT)
                    .background(HhPaperColors.Page, RoundedCornerShape(HhTheme.spacing.d4 + HhTheme.spacing.d2))
                    .padding(HhTheme.spacing.d20),
            ) {
                ExportPaper(sheet = sheet, scrollState = paperScroll)
            }

            uiState.stage == ExportPreviewStage.RENDERING -> PagePlaceholder(modifier = widthModifier) {
                RenderingSteps()
            }

            else -> PagePlaceholder(modifier = widthModifier) {
                Icon(
                    imageVector = HhIcons.Description,
                    contentDescription = null,
                    tint = HhTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(HhTheme.spacing.xxxl),
                )
                Text(
                    text = stringResource(R.string.feature_tailor_impl_export_preview_failed_placeholder),
                    style = HhTheme.typography.labelL,
                    color = HhTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text(
            text = pageCaption(uiState, paperScroll.canScrollForward),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun pageCaption(uiState: ExportPreviewUiState, canScrollForward: Boolean): String = when {
    uiState.sheet == null && uiState.stage == ExportPreviewStage.RENDERING ->
        stringResource(R.string.feature_tailor_impl_export_preview_rendering_caption)

    uiState.sheet == null -> stringResource(R.string.feature_tailor_impl_export_preview_no_preview)

    else -> stringResource(
        R.string.feature_tailor_impl_export_preview_paper_meta,
        uiState.format.label(),
        stringResource(
            if (canScrollForward) {
                R.string.feature_tailor_impl_export_preview_paper_caption_scroll
            } else {
                R.string.feature_tailor_impl_export_preview_paper_caption_end
            },
        ),
    )
}

@Composable
private fun PagePlaceholder(modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .heightIn(min = HhTheme.spacing.d64 * PLACEHOLDER_HEIGHT_UNITS)
            .background(HhTheme.colors.card, RoundedCornerShape(HhTheme.spacing.d4 + HhTheme.spacing.d2))
            .border(
                HhTheme.spacing.d2,
                HhTheme.colors.outlineVariant,
                RoundedCornerShape(HhTheme.spacing.d4 + HhTheme.spacing.d2),
            )
            .padding(HhTheme.spacing.d20),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md, Alignment.CenterVertically),
    ) {
        content()
    }
}

@Composable
private fun RenderingSteps() {
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
            .background(HhTheme.colors.card, HhTheme.shapes.field)
            .defaultMinSize(minHeight = HhTheme.spacing.d64)
            .padding(horizontal = HhTheme.spacing.d12 + HhTheme.spacing.xxs, vertical = HhTheme.spacing.d12),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = HhIcons.Description,
            contentDescription = null,
            tint = HhTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(HhTheme.spacing.xxl),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_export_preview_file_label),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
            Text(text = uiState.fileName, style = HhTheme.typography.labelL, color = HhTheme.colors.onSurface)
        }
    }
    if (uiState.stage == ExportPreviewStage.PREVIEW_READY) {
        NoteLine(
            text = stringResource(R.string.feature_tailor_impl_export_preview_ats_note),
            icon = HhIcons.Check,
        )
    }
}

@Composable
private fun ExportPreviewDownloadBar(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
    enabled: Boolean,
) {
    val creditLine = exportPreviewCreditLine(uiState)
    HhBottomActionBar(
        creditDisclosure = if (creditLine.isEmpty()) {
            null
        } else {
            {
                Text(
                    text = creditLine,
                    style = HhTheme.typography.bodyS,
                    color = HhTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
    ) {
        HhPrimaryButton(
            label = stringResource(
                when (uiState.format) {
                    ExportFormat.PDF -> R.string.feature_tailor_impl_export_preview_download_pdf
                    ExportFormat.DOCX -> R.string.feature_tailor_impl_export_preview_download_docx
                },
            ),
            onClick = if (uiState.needsCredits) actions.onBuyCredits else actions.onExport,
            modifier = Modifier.weight(1f),
            enabled = enabled,
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
private fun ExportingSheet(uiState: ExportPreviewUiState) {
    val colors = HhTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.sheet, HhTheme.shapes.modalSheet)
            .navigationBarsPadding()
            .padding(
                start = HhTheme.spacing.gutter,
                end = HhTheme.spacing.gutter,
                top = HhTheme.spacing.d12,
                bottom = HhTheme.spacing.gutter,
            ),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = HhTheme.spacing.d32 + HhTheme.spacing.d4, height = HhTheme.spacing.d4)
                .background(colors.outlineVariant, HhTheme.shapes.pill),
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_exporting_title),
            style = HhTheme.typography.headlineM,
            color = colors.onSurface,
        )
        HhStepProgress(
            modifier = Modifier.fillMaxWidth(),
            stepNames = listOf(
                stringResource(R.string.feature_tailor_impl_export_preview_step_making, uiState.format.label()),
                stringResource(R.string.feature_tailor_impl_export_preview_step_saving),
            ),
            currentStepIndex = 0,
            ordinalLabel = stringResource(
                if (uiState.isFreeBeta) {
                    R.string.feature_tailor_impl_export_preview_exporting_eyebrow
                } else {
                    R.string.feature_tailor_impl_export_preview_exporting_eyebrow_credit
                },
            ),
            stepStatuses = listOf(
                stringResource(R.string.feature_tailor_impl_export_preview_step_status_active),
                stringResource(R.string.feature_tailor_impl_export_preview_step_status_waiting),
            ),
            footnote = stringResource(R.string.feature_tailor_impl_export_preview_exporting_footnote),
        )
    }
}

@Composable
private fun exportPreviewCreditLine(uiState: ExportPreviewUiState): String = when {
    uiState.isOffline || uiState.isFreeBeta || !uiState.creditsKnown || uiState.needsCredits -> ""
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
