package com.hirehop.feature.tailor.impl.exportpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhDividerStyle
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.document.SKILLS_HEADING

internal const val EXPORT_PREVIEW_FORMAT_GROUP = "exportPreviewFormatGroup"

private val PAGE_SURFACE = Color(0xFFFFFFFF)

private val PAGE_INK = Color(0xFF111111)

private const val PAGE_ASPECT = 0.707f

@Composable
internal fun ExportPreviewScreen(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_tailor_impl_export_preview_title),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                navigationIconContentDescription = stringResource(
                    R.string.feature_tailor_impl_export_preview_navigation_back_description,
                ),
                onNavigationClick = actions.onNavigateBack,
            )
        },
        bottomBar = {
            when (uiState.stage) {
                ExportPreviewStage.PREVIEW_READY,
                ExportPreviewStage.OFFLINE,
                -> ExportPreviewBottomBar(uiState = uiState, actions = actions)

                else -> Unit
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ExportPreviewBody(uiState = uiState, actions = actions)
        }
    }
}

@Composable
private fun ExportPreviewBody(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
) {
    when (uiState.stage) {
        ExportPreviewStage.IDLE,
        ExportPreviewStage.RENDERING,
        -> ExportPreviewRendering(uiState = uiState)

        ExportPreviewStage.PREVIEW_READY,
        ExportPreviewStage.NO_CREDIT,
        ExportPreviewStage.OFFLINE,
        -> ExportPreviewReady(uiState = uiState, actions = actions)

        ExportPreviewStage.PREVIEW_FAILED -> ExportPreviewFailed(actions = actions)
        ExportPreviewStage.NO_DOCUMENT -> ExportPreviewNoDocument()
        ExportPreviewStage.EXPORTING -> ExportPreviewExporting(uiState = uiState)
        ExportPreviewStage.EXPORT_SUCCEEDED -> ExportPreviewExported(uiState = uiState, actions = actions)
        ExportPreviewStage.EXPORT_FAILED -> ExportPreviewExportFailed(uiState = uiState, actions = actions)
    }
}

@Composable
private fun ExportPreviewBottomBar(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
) {
    HhBottomActionBar(
        actions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            ) {
                ExportPreviewCreditLine(uiState = uiState)
                HhButton(
                    onClick = if (uiState.needsCredits) actions.onBuyCredits else actions.onExport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = HhTheme.spacing.d48),
                    enabled = uiState.canExport || uiState.needsCredits,
                    text = {
                        Text(
                            text = stringResource(uiState.format.downloadLabelRes()),
                            style = HhTheme.typography.labelLarge,
                        )
                    },
                )
            }
        },
    )
}

@Composable
private fun ExportPreviewCreditLine(uiState: ExportPreviewUiState) {
    if (!uiState.creditKnown) return
    val noCredit = uiState.needsCredits
    Text(
        text = when {
            noCredit && uiState.showsPackPrice -> stringResource(
                R.string.feature_tailor_impl_export_preview_credit_none_with_price,
                uiState.packPrice,
            )

            noCredit -> stringResource(R.string.feature_tailor_impl_export_preview_credit_none)
            uiState.isFreeCredit -> pluralStringResource(
                R.plurals.feature_tailor_impl_export_preview_credit_free,
                uiState.creditsLeft,
                uiState.creditsLeft,
            )

            else -> pluralStringResource(
                R.plurals.feature_tailor_impl_export_preview_credit_paid,
                uiState.creditsLeft,
                uiState.creditsLeft,
            )
        },
        style = HhTheme.typography.bodySmall,
        color = HhTheme.colors.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ExportPreviewRendering(uiState: ExportPreviewUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d16, vertical = HhTheme.spacing.d16),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_tailor_impl_export_preview_offline_message),
            supportingText = stringResource(R.string.feature_tailor_impl_export_preview_offline_supporting),
            visible = uiState.isOffline,
        )
        ExportPreviewHeading(uiState = uiState)
        HhStepProgress(
            stepNames = listOf(stringResource(R.string.feature_tailor_impl_export_preview_step_setting_page)),
            currentStepIndex = 0,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_rendering_note),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        ExportPreviewPageSkeleton()
    }
}

@Composable
private fun ExportPreviewPageSkeleton() {
    val description = stringResource(R.string.feature_tailor_impl_export_preview_rendering_skeleton_description)
    Column(
        modifier = Modifier
            .widthIn(max = PAGE_MAX_WIDTH)
            .fillMaxWidth()
            .aspectRatio(PAGE_ASPECT)
            .background(color = PAGE_SURFACE, shape = RoundedCornerShape(HhTheme.shapes.sm))
            .border(
                width = 1.dp,
                color = HhTheme.colors.hairline,
                shape = RoundedCornerShape(HhTheme.shapes.sm),
            )
            .padding(HhTheme.spacing.d20)
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        repeat(SKELETON_LINE_COUNT) { index ->
            HhDivider(
                modifier = Modifier.fillMaxWidth(if (index % 2 == 0) 1f else 0.6f),
                style = HhDividerStyle.Dashed,
                color = PAGE_INK.copy(alpha = SKELETON_INK_ALPHA),
            )
        }
    }
}

@Composable
private fun ExportPreviewReady(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d16, vertical = HhTheme.spacing.d16),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_tailor_impl_export_preview_offline_message),
            supportingText = stringResource(R.string.feature_tailor_impl_export_preview_offline_supporting),
            visible = uiState.isOffline,
        )
        ExportPreviewHeading(uiState = uiState)
        ExportPreviewFormatSelector(uiState = uiState, onSelectFormat = actions.onSelectFormat)
        val sheet = uiState.sheet
        if (sheet != null) {
            ExportPreviewPage(sheet = sheet, fileName = uiState.fileName)
            ExportPreviewFileLine(sheet = sheet, fileName = uiState.fileName)
        }
    }
}

@Composable
private fun ExportPreviewHeading(uiState: ExportPreviewUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_label),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_notice),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        if (uiState.jobTitle.isNotBlank() || uiState.jobCompany.isNotBlank()) {
            Text(
                text = stringResource(
                    R.string.feature_tailor_impl_export_preview_subtitle,
                    uiState.jobTitle,
                    uiState.jobCompany,
                ),
                style = HhTheme.typography.titleMedium,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExportPreviewFormatSelector(
    uiState: ExportPreviewUiState,
    onSelectFormat: (ExportFormat) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_format_label),
            style = HhTheme.typography.labelMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            ExportFormat.entries.forEach { format ->
                ExportPreviewFormatChip(
                    format = format,
                    selected = uiState.format == format,
                    enabled = uiState.stage != ExportPreviewStage.EXPORTING,
                    onSelect = onSelectFormat,
                )
            }
        }
    }
}

@Composable
private fun ExportPreviewFormatChip(
    format: ExportFormat,
    selected: Boolean,
    enabled: Boolean,
    onSelect: (ExportFormat) -> Unit,
) {
    val label = format.label()
    val shape = RoundedCornerShape(HhTheme.shapes.sm)
    val announcement = stringResource(
        if (selected) {
            R.string.feature_tailor_impl_export_preview_format_selected
        } else {
            R.string.feature_tailor_impl_export_preview_format_unselected
        },
        label,
    )
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = HhTheme.spacing.d48)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = { onSelect(format) },
            )
            .background(
                color = if (selected) HhTheme.colors.primaryContainer else HhTheme.colors.surface,
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = if (selected) HhTheme.colors.primaryContainer else HhTheme.colors.hairline,
                shape = shape,
            )
            .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.sm)
            .clearAndSetSemantics { contentDescription = announcement },
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = HhTheme.typography.labelLarge,
            color = if (selected) HhTheme.colors.onPrimaryContainer else HhTheme.colors.onSurface,
        )
        if (format.isLaterRelease) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_export_preview_format_later_release),
                style = HhTheme.typography.monoSmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExportPreviewPage(
    sheet: ExportPreviewSheet,
    fileName: String,
) {
    val pageDescription = stringResource(
        R.string.feature_tailor_impl_export_preview_file_name_description,
        fileName,
    )
    Column(
        modifier = Modifier
            .widthIn(max = PAGE_MAX_WIDTH)
            .fillMaxWidth()
            .heightIn(max = PAGE_MAX_HEIGHT)
            .background(color = PAGE_SURFACE, shape = RoundedCornerShape(HhTheme.shapes.sm))
            .border(
                width = 1.dp,
                color = HhTheme.colors.hairline,
                shape = RoundedCornerShape(HhTheme.shapes.sm),
            )
            .verticalScroll(rememberScrollState())
            .padding(HhTheme.spacing.d20)
            .clearAndSetSemantics { contentDescription = pageDescription },
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        ExportPreviewPageSheet(sheet = sheet)
    }
}

@Composable
private fun ExportPreviewPageSheet(sheet: ExportPreviewSheet) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        Text(
            text = sheet.name,
            style = HhTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
            color = PAGE_INK,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        if (sheet.contactLine.isNotBlank()) {
            Text(
                text = sheet.contactLine,
                style = HhTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                color = PAGE_INK,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (sheet.headline.isNotBlank()) {
            Text(
                text = sheet.headline,
                style = HhTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                color = PAGE_INK,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        sheet.sections.forEach { section -> ExportPreviewPageSection(section = section) }
        if (sheet.skills.isNotEmpty()) {
            ExportPreviewPageSection(
                section = ExportPreviewSection(
                    heading = SKILLS_HEADING,
                    entries = listOf(
                        ExportPreviewEntry(
                            title = sheet.skills.joinToString(),
                            organization = "",
                            dateRange = "",
                            bullets = emptyList(),
                        ),
                    ),
                ),
            )
        }
    }
}

@Composable
private fun ExportPreviewPageSection(section: ExportPreviewSection) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        Text(
            text = section.heading.uppercase(),
            style = HhTheme.typography.labelMedium.copy(fontFamily = FontFamily.Serif),
            color = PAGE_INK,
        )
        HhDivider(thickness = 1.dp, color = PAGE_INK.copy(alpha = RULE_INK_ALPHA))
        section.entries.forEach { entry ->
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
                Text(
                    text = listOf(entry.title, entry.organization, entry.dateRange)
                        .filter { part -> part.isNotBlank() }
                        .joinToString(" · "),
                    style = HhTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                    color = PAGE_INK,
                )
                entry.bullets.forEach { bullet ->
                    Text(
                        text = bullet,
                        style = HhTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                        color = PAGE_INK,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportPreviewFileLine(
    sheet: ExportPreviewSheet,
    fileName: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
        if (fileName.isNotBlank()) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_export_preview_file_name_label),
                style = HhTheme.typography.monoSmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
            Text(
                text = fileName,
                style = HhTheme.typography.mono,
                color = HhTheme.colors.onSurface,
            )
        }
        val lineCount = pluralStringResource(
            R.plurals.feature_tailor_impl_export_preview_line_count,
            sheet.lineCount,
            sheet.lineCount,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_line_count_caption, lineCount),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_page_note),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_ats_note),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExportPreviewFailed(actions: ExportPreviewActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Error)
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_error_heading),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_error_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhDivider(style = HhDividerStyle.Dashed)
        HhButton(
            onClick = actions.onRetryPreview,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_export_preview_error_retry),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}

@Composable
private fun ExportPreviewNoDocument() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_empty_heading),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_empty_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExportPreviewExporting(uiState: ExportPreviewUiState) {
    val formatLabel = uiState.format.announcedLabel()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d16, vertical = HhTheme.spacing.d16),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhStepProgress(
            stepNames = listOf(
                stringResource(R.string.feature_tailor_impl_export_preview_exporting_heading, formatLabel),
            ),
            currentStepIndex = 0,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_exporting_note),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExportPreviewExported(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
) {
    val formatLabel = (uiState.exportedFormat ?: uiState.format).announcedLabel()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d16, vertical = HhTheme.spacing.d16),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Done)
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_exported_heading, formatLabel),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
        )
        if (uiState.fileName.isNotBlank()) {
            Text(
                text = uiState.fileName,
                style = HhTheme.typography.mono,
                color = HhTheme.colors.onSurface,
            )
        }
        Text(
            text = stringResource(R.string.feature_tailor_impl_export_preview_exported_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhCard {
            Text(
                text = stringResource(R.string.feature_tailor_impl_export_preview_page_note),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        HhButton(
            onClick = { actions.onExported(uiState.exportedFormat ?: uiState.format) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_export_preview_exported_open),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
        HhButton(
            onClick = actions.onDismissResult,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_export_preview_exported_dismiss),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}

@Composable
private fun ExportPreviewExportFailed(
    uiState: ExportPreviewUiState,
    actions: ExportPreviewActions,
) {
    val sheet = uiState.sheet
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d16, vertical = HhTheme.spacing.d16),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhErrorCallout(
            title = stringResource(R.string.feature_tailor_impl_export_preview_export_failed_heading),
            supportingText = stringResource(R.string.feature_tailor_impl_export_preview_export_failed_body),
        )
        if (sheet != null) {
            ExportPreviewPage(sheet = sheet, fileName = uiState.fileName)
        }
        HhButton(
            onClick = actions.onExport,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
            enabled = uiState.hasSheet,
            text = {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_export_preview_export_failed_retry),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}

@Composable
private fun ExportFormat.label(): String = stringResource(
    when (this) {
        ExportFormat.PDF -> R.string.feature_tailor_impl_export_preview_format_pdf
        ExportFormat.DOCX -> R.string.feature_tailor_impl_export_preview_format_docx
    },
)

@Composable
private fun ExportFormat.announcedLabel(): String = label()

private fun ExportFormat.downloadLabelRes(): Int = when (this) {
    ExportFormat.PDF -> R.string.feature_tailor_impl_export_preview_download_pdf
    ExportFormat.DOCX -> R.string.feature_tailor_impl_export_preview_download_docx
}

private val PAGE_MAX_WIDTH = 320.dp

private val PAGE_MAX_HEIGHT = 520.dp

private const val SKELETON_LINE_COUNT = 7

private const val SKELETON_INK_ALPHA = 0.28f

private const val RULE_INK_ALPHA = 0.7f
