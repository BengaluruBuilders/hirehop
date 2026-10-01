package com.hirehop.feature.tailor.impl.coverletter

import android.content.ClipData
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhConfirmSheet
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhDividerStyle
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSectionCard
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.ui.FactIdTag
import com.hirehop.feature.tailor.impl.R
import kotlinx.coroutines.launch

private const val HH_STACK_FONT_SCALE = 1.5f

private const val HH_CLIP_LABEL = "HireHop cover letter"

@Composable
internal fun CoverLetterScreen(
    uiState: CoverLetterUiState,
    actions: CoverLetterActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_tailor_impl_cover_letter_title),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                navigationIconContentDescription = stringResource(
                    R.string.feature_tailor_impl_cover_letter_navigation_back_description,
                ),
                onNavigationClick = actions.onNavigateBack,
            )
        },
        bottomBar = {
            if (uiState.paragraphs.isNotEmpty()) {
                CoverLetterBottomBar(uiState = uiState, actions = actions)
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState.stage) {
                CoverLetterStage.GENERATING -> CoverLetterGenerating()
                CoverLetterStage.ERROR -> CoverLetterError(actions = actions)
                CoverLetterStage.EMPTY_PROFILE -> CoverLetterEmptyProfile()
                CoverLetterStage.READY,
                CoverLetterStage.NO_MATCHING_EVIDENCE,
                CoverLetterStage.OFFLINE,
                -> CoverLetterBody(uiState = uiState, actions = actions)
            }
        }
    }
    if (uiState.isEditing) {
        CoverLetterEditSheet(uiState = uiState, actions = actions)
    }
}

@Composable
private fun CoverLetterBottomBar(
    uiState: CoverLetterUiState,
    actions: CoverLetterActions,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    HhBottomActionBar(
        actions = {
            HhOutlinedButton(
                onClick = actions.onSkipLetter,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = HhTheme.spacing.d48),
                text = {
                    Text(
                        text = stringResource(R.string.feature_tailor_impl_cover_letter_skip_action),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
            HhButton(
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(HH_CLIP_LABEL, uiState.letterText)))
                    }
                    actions.onCopyLetter(uiState.letterText)
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = HhTheme.spacing.d48),
                text = {
                    Text(
                        text = stringResource(R.string.feature_tailor_impl_cover_letter_copy_action),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
        },
    )
}

@Composable
private fun CoverLetterGenerating() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        HhStepProgress(
            stepNames = listOf(
                stringResource(R.string.feature_tailor_impl_cover_letter_step_pick),
                stringResource(R.string.feature_tailor_impl_cover_letter_step_write),
                stringResource(R.string.feature_tailor_impl_cover_letter_step_check),
            ),
            currentStepIndex = 1,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_generating_note),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun CoverLetterError(actions: CoverLetterActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Error)
        Text(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_error_heading),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_error_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhDivider(style = HhDividerStyle.Dashed)
        HhButton(
            onClick = actions.onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_cover_letter_error_retry),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}

@Composable
private fun CoverLetterEmptyProfile() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        Text(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_empty_profile_heading),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_empty_profile_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun CoverLetterBody(
    uiState: CoverLetterUiState,
    actions: CoverLetterActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d16, vertical = HhTheme.spacing.d16),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_tailor_impl_cover_letter_offline_message),
            supportingText = stringResource(R.string.feature_tailor_impl_cover_letter_offline_supporting),
            visible = uiState.isOffline,
        )
        CoverLetterMessageNote(uiState = uiState, actions = actions)
        if (uiState.paragraphs.isNotEmpty()) {
            CoverLetterWordCount(uiState = uiState)
        }
        if (uiState.stage == CoverLetterStage.NO_MATCHING_EVIDENCE) {
            NoMatchingEvidenceNote(
                evidenceText = uiState.paragraphs
                    .firstOrNull { paragraph -> paragraph.basis == CoverLetterBasis.NO_MATCHING_EVIDENCE }
                    ?.text
                    .orEmpty(),
            )
        }
        uiState.paragraphs.forEach { paragraph ->
            CoverLetterParagraphCard(
                paragraph = paragraph,
                total = uiState.paragraphs.size,
                onEdit = actions.onBeginEdit,
                onReport = actions.onReportInaccurate,
            )
        }
    }
}

@Composable
private fun CoverLetterMessageNote(
    uiState: CoverLetterUiState,
    actions: CoverLetterActions,
) {
    val message = uiState.message ?: return
    HhCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            Text(
                text = stringResource(message.labelRes()),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            CoverLetterTextLink(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_dismiss),
                onClick = actions.onDismissMessage,
            )
        }
    }
}

@Composable
private fun CoverLetterWordCount(uiState: CoverLetterUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = uiState.wordCount.toString(),
            style = HhTheme.typography.heroNumeral,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_words_caption),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        if (!isWithinWordTarget(uiState.wordCount)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_cover_letter_word_count_target),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NoMatchingEvidenceNote(evidenceText: String) {
    HhSectionCard {
        Text(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_no_evidence_heading),
            style = HhTheme.typography.titleLarge,
            color = HhTheme.colors.onSurface,
        )
        if (evidenceText.isNotEmpty()) {
            Text(
                text = evidenceText,
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurface,
            )
        }
        Text(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_no_evidence_note),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoverLetterParagraphCard(
    paragraph: CoverLetterParagraph,
    total: Int,
    onEdit: (Int) -> Unit,
    onReport: (Int) -> Unit,
) {
    HhCard {
        Text(
            text = stringResource(
                R.string.feature_tailor_impl_cover_letter_paragraph_heading,
                paragraph.ordinal,
                total,
            ),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        CoverLetterSentences(paragraph = paragraph)
        if (paragraph.isUserEdited) {
            HhProvenanceChip(kind = HhProvenanceKind.UserEdited)
        }
        CoverLetterBasisLine(paragraph = paragraph)
        if (paragraph.isUserEdited) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_cover_letter_edit_note),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        CoverLetterFactRow(paragraph = paragraph)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhOutlinedButton(
                onClick = { onEdit(paragraph.ordinal) },
                modifier = Modifier.heightIn(min = HhTheme.spacing.d48),
                text = {
                    Text(
                        text = stringResource(R.string.feature_tailor_impl_cover_letter_edit_action),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
            CoverLetterTextLink(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_report_action),
                onClick = { onReport(paragraph.ordinal) },
            )
        }
    }
}

@Composable
private fun CoverLetterBasisLine(paragraph: CoverLetterParagraph) {
    val firstFact = paragraph.facts.firstOrNull()
    val text = if (paragraph.isUserEdited && firstFact != null) {
        stringResource(R.string.feature_tailor_impl_cover_letter_basis_edited_fact, firstFact.factId)
    } else {
        stringResource(paragraph.basis.labelRes())
    }
    Text(
        text = text,
        style = HhTheme.typography.bodySmall,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun CoverLetterSentences(paragraph: CoverLetterParagraph) {
    val stacked = LocalDensity.current.fontScale > HH_STACK_FONT_SCALE
    val sentences: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            paragraph.sentences.forEach { sentence ->
                Text(
                    text = sentence.text,
                    style = HhTheme.typography.bodyLarge,
                    color = HhTheme.colors.onSurface,
                )
            }
        }
    }
    val citations: @Composable () -> Unit = {
        Column {
            paragraph.facts.forEach { fact ->
                FactIdTag(factId = fact.factId)
            }
        }
    }
    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            sentences()
            citations()
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.weight(1f)) { sentences() }
            citations()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoverLetterFactRow(paragraph: CoverLetterParagraph) {
    if (paragraph.facts.isEmpty()) return
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        paragraph.facts.forEach { fact ->
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
                FactIdTag(factId = fact.factId)
                Text(
                    text = fact.entryTitle,
                    style = HhTheme.typography.monoSmall,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CoverLetterTextLink(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = HhTheme.typography.labelLarge,
        color = HhTheme.colors.onSurfaceVariant,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .defaultMinSize(minHeight = HhTheme.spacing.d48)
            .clickable(onClick = onClick)
            .padding(vertical = HhTheme.spacing.sm),
    )
}

@Composable
private fun CoverLetterEditSheet(
    uiState: CoverLetterUiState,
    actions: CoverLetterActions,
) {
    HhConfirmSheet(
        title = stringResource(R.string.feature_tailor_impl_cover_letter_edit_sheet_title),
        message = stringResource(R.string.feature_tailor_impl_cover_letter_edit_sheet_message),
        confirmLabel = stringResource(R.string.feature_tailor_impl_cover_letter_edit_save),
        cancelLabel = stringResource(R.string.feature_tailor_impl_cover_letter_edit_cancel),
        onConfirm = actions.onSaveEdit,
        onCancel = actions.onCancelEdit,
        content = {
            HhTextField(
                value = uiState.editingText,
                onValueChange = actions.onEditTextChanged,
                label = stringResource(R.string.feature_tailor_impl_cover_letter_edit_field_label),
                singleLine = false,
                minLines = 4,
            )
        },
    )
}

private fun CoverLetterMessage.labelRes(): Int = when (this) {
    CoverLetterMessage.COPIED -> R.string.feature_tailor_impl_cover_letter_copied
    CoverLetterMessage.SAVED -> R.string.feature_tailor_impl_cover_letter_saved
    CoverLetterMessage.REPORT_UNAVAILABLE -> R.string.feature_tailor_impl_cover_letter_report_unavailable
}

private fun CoverLetterBasis.labelRes(): Int = when (this) {
    CoverLetterBasis.CONFIRMED_FACT -> R.string.feature_tailor_impl_cover_letter_basis_fact
    CoverLetterBasis.JOB_DESCRIPTION -> R.string.feature_tailor_impl_cover_letter_basis_job
    CoverLetterBasis.PROFILE_NAME -> R.string.feature_tailor_impl_cover_letter_basis_name
    CoverLetterBasis.NO_MATCHING_EVIDENCE -> R.string.feature_tailor_impl_cover_letter_basis_no_evidence
    CoverLetterBasis.PLAIN -> R.string.feature_tailor_impl_cover_letter_basis_plain
}
