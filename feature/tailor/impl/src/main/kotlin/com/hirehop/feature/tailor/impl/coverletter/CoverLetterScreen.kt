package com.hirehop.feature.tailor.impl.coverletter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhBottomSheet
import com.hirehop.core.designsystem.component.HhEvidenceText
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.HhToastHost
import com.hirehop.core.designsystem.component.evidenceMarkSpanStyle
import com.hirehop.core.designsystem.component.rememberHhToastState
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.tailor.impl.BulletReviewState
import com.hirehop.feature.tailor.impl.DecisionChip
import com.hirehop.feature.tailor.impl.NoticeStrip
import com.hirehop.feature.tailor.impl.ProgressCard
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.SourceBadge
import com.hirehop.feature.tailor.impl.StatusCard
import com.hirehop.feature.tailor.impl.jobLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CoverLetterScreen(
    uiState: CoverLetterUiState,
    actions: CoverLetterActions,
    modifier: Modifier = Modifier,
    initialSourceOrdinal: Int? = null,
) {
    var sourceOrdinal by remember { mutableStateOf(initialSourceOrdinal) }
    val toastState = rememberHhToastState()
    val message = uiState.message
    val messageText = message?.let { stringResource(it.textRes()) }
    LaunchedEffect(message) {
        if (messageText == null) return@LaunchedEffect
        toastState.show(messageText)
        actions.onDismissMessage()
    }
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
                title = stringResource(R.string.feature_tailor_impl_cover_letter_title),
                subtitle = uiState.subtitle(),
                onBack = actions.onNavigateBack,
                backContentDescription = stringResource(R.string.feature_tailor_impl_cover_letter_back),
            )
        },
        bottomBar = { CoverLetterBottomBar(uiState, actions) },
        snackbarHost = { HhToastHost(toastState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = HhTheme.spacing.gutter,
                end = HhTheme.spacing.gutter,
                top = padding.calculateTopPadding() + HhTheme.spacing.sm,
                bottom = padding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.d2),
        ) {
            coverLetterItems(uiState, actions, sourceOrdinal) { sourceOrdinal = it }
        }
    }
    val sourceParagraph = uiState.paragraphs.firstOrNull { it.ordinal == sourceOrdinal }
    if (sourceParagraph != null) {
        HhBottomSheet(onDismissRequest = { sourceOrdinal = null }) {
            ParagraphSources(
                position = uiState.positionOf(sourceParagraph),
                paragraph = sourceParagraph,
                onClose = { sourceOrdinal = null },
            )
        }
    }
}

@Composable
private fun CoverLetterUiState.subtitle(): String? = jobLine(jobTitle, jobCompany)

private fun CoverLetterMessage.textRes(): Int = when (this) {
    CoverLetterMessage.SAVED -> R.string.feature_tailor_impl_cover_letter_saved
    CoverLetterMessage.REPORTED -> R.string.feature_tailor_impl_report_thanks
}

private fun CoverLetterUiState.positionOf(paragraph: CoverLetterParagraph): Int =
    paragraphs.filter { !it.isGreeting }.indexOfFirst { it.ordinal == paragraph.ordinal } + 1

private fun LazyListScope.coverLetterItems(
    uiState: CoverLetterUiState,
    actions: CoverLetterActions,
    sourceOrdinal: Int?,
    onSource: (Int) -> Unit,
) {
    if (uiState.isOffline && uiState.stage != CoverLetterStage.GENERATING) {
        item(key = "offline") {
            NoticeStrip(text = stringResource(R.string.feature_tailor_impl_cover_letter_offline), icon = HhIcons.Offline)
        }
    }
    when (uiState.stage) {
        CoverLetterStage.OFFER -> {
            item(key = "progress") { ReviewedStrip(uiState) }
            item(key = "offer") { OfferCard() }
        }
        CoverLetterStage.GENERATING -> item(key = "generating") { GeneratingCard(uiState) }
        CoverLetterStage.READY, CoverLetterStage.NO_MATCHING_EVIDENCE -> {
            if (uiState.stage == CoverLetterStage.NO_MATCHING_EVIDENCE) {
                item(key = "no-evidence") {
                    NoticeStrip(
                        text = stringResource(R.string.feature_tailor_impl_cover_letter_no_evidence_note),
                        icon = HhIcons.Verified,
                    )
                }
            }
            item(key = "letter") { LetterCard(uiState, actions, sourceOrdinal, onSource) }
        }
        CoverLetterStage.EMPTY_PROFILE -> item(key = "empty") {
            StatusCard(
                kind = HhSpotKind.Empty,
                title = stringResource(R.string.feature_tailor_impl_cover_letter_empty_profile_heading),
                body = stringResource(R.string.feature_tailor_impl_cover_letter_empty_profile_body),
            )
        }
        CoverLetterStage.ERROR -> item(key = "error") {
            StatusCard(
                kind = HhSpotKind.Error,
                title = stringResource(R.string.feature_tailor_impl_cover_letter_error_title),
                body = stringResource(R.string.feature_tailor_impl_cover_letter_error_body),
            )
        }
    }
}

@Composable
private fun ReviewedStrip(uiState: CoverLetterUiState) {
    if (uiState.totalCount <= 0) return
    ProgressCard(
        reviewed = uiState.reviewedCount,
        total = uiState.totalCount,
        flagged = 0,
        showAllReviewedNote = false,
    )
}

@Composable
private fun OfferCard() {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.d16 + HhTheme.spacing.xxs)) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_cover_letter_offer_title),
                style = HhTheme.typography.titleL,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_cover_letter_offer_body),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
        }
    }
}

@Composable
private fun GeneratingCard(uiState: CoverLetterUiState) {
    val picked = if (uiState.paragraphCount > 0) {
        stringResource(
            R.string.feature_tailor_impl_cover_letter_step_pick_detail,
            uiState.factCount,
            uiState.paragraphCount,
        )
    } else {
        null
    }
    HhStepProgress(
        stepNames = listOf(
            stringResource(R.string.feature_tailor_impl_cover_letter_step_pick),
            stringResource(R.string.feature_tailor_impl_cover_letter_step_write),
            stringResource(R.string.feature_tailor_impl_cover_letter_step_check),
        ),
        currentStepIndex = 1,
        ordinalLabel = stringResource(R.string.feature_tailor_impl_loading_caption),
        stepDetails = listOf(picked, null, null),
        footnote = stringResource(R.string.feature_tailor_impl_cover_letter_footnote),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LetterCard(
    uiState: CoverLetterUiState,
    actions: CoverLetterActions,
    sourceOrdinal: Int?,
    onSource: (Int) -> Unit,
) {
    HhHeroCard {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.d2)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
                itemVerticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_cover_letter_label),
                    style = HhTheme.typography.labelM,
                    color = HhTheme.colors.onSurfaceVariant,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.feature_tailor_impl_cover_letter_words,
                        uiState.wordCount,
                        uiState.wordCount,
                    ),
                    style = HhTheme.typography.numeralM,
                    color = HhTheme.colors.onSurface,
                )
            }
            uiState.paragraphs.forEach { paragraph ->
                if (paragraph.isGreeting) {
                    Text(text = paragraph.text, style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurface)
                } else {
                    ParagraphBlock(
                        paragraph = paragraph,
                        position = uiState.positionOf(paragraph),
                        uiState = uiState,
                        actions = actions,
                        marked = sourceOrdinal == paragraph.ordinal,
                        onSource = { onSource(paragraph.ordinal) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ParagraphBlock(
    paragraph: CoverLetterParagraph,
    position: Int,
    uiState: CoverLetterUiState,
    actions: CoverLetterActions,
    marked: Boolean,
    onSource: () -> Unit,
) {
    val editing = uiState.editingOrdinal == paragraph.ordinal
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs)) {
            if (editing) {
                EditingBlock(position, uiState, actions)
            } else {
                HhEvidenceText(
                    text = paragraph.annotated(marked, evidenceMarkSpanStyle()),
                    style = HhTheme.typography.bodyM,
                )
                ParagraphNotes(paragraph)
                ParagraphActions(
                    paragraph = paragraph,
                    isReported = paragraph.ordinal.toString() in uiState.reportedIds,
                    actions = actions,
                )
            }
        }
        SourceBadge(
            factIds = paragraph.facts.map { it.displayId }.distinct(),
            onClick = onSource,
            selected = marked,
        )
    }
}

private fun CoverLetterParagraph.annotated(marked: Boolean, style: androidx.compose.ui.text.SpanStyle): AnnotatedString {
    if (!marked || isUserEdited) return AnnotatedString(text)
    return buildAnnotatedString {
        sentences.forEachIndexed { index, sentence ->
            if (index > 0) append(" ")
            if (sentence.factId != null) withStyle(style) { append(sentence.text) } else append(sentence.text)
        }
    }
}

@Composable
private fun ParagraphNotes(paragraph: CoverLetterParagraph) {
    paragraph.flag?.let { flag ->
        NoticeStrip(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_flag_note, flag.quote, flag.factId),
            icon = HhIcons.Flag,
        )
    }
    if (paragraph.isUserEdited) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DecisionChip(BulletReviewState.USER_EDITED)
            Text(
                text = stringResource(R.string.feature_tailor_impl_cover_letter_user_edited_note),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ParagraphActions(paragraph: CoverLetterParagraph, isReported: Boolean, actions: CoverLetterActions) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        HhTextButton(
            label = stringResource(
                if (isReported) R.string.feature_tailor_impl_menu_reported else R.string.feature_tailor_impl_cover_letter_report,
            ),
            onClick = { actions.onReportInaccurate(paragraph.ordinal) },
            enabled = !isReported,
            leadingIcon = HhIcons.Flag,
        )
        HhTextButton(
            label = stringResource(R.string.feature_tailor_impl_cover_letter_edit),
            onClick = { actions.onBeginEdit(paragraph.ordinal) },
            leadingIcon = HhIcons.Edit,
        )
    }
}

@Composable
private fun EditingBlock(position: Int, uiState: CoverLetterUiState, actions: CoverLetterActions) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhTextField(
            value = uiState.editingText,
            onValueChange = actions.onEditTextChanged,
            label = stringResource(R.string.feature_tailor_impl_cover_letter_edit_label, position),
            singleLine = false,
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        NoticeStrip(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_user_edited_note),
            icon = HhIcons.Verified,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhOutlineButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_edit_cancel),
                onClick = actions.onCancelEdit,
                modifier = Modifier.weight(1f),
            )
            HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_edit_save),
                onClick = actions.onSaveEdit,
                enabled = uiState.editingText.isNotBlank(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun ParagraphSources(position: Int, paragraph: CoverLetterParagraph, onClose: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_sources_title, position),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
        paragraph.facts.forEach { fact ->
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = fact.displayId, style = HhTheme.typography.factId, color = HhTheme.colors.primary)
                    HhProvenanceChip(
                        kind = com.hirehop.core.designsystem.component.HhProvenanceKind.Confirmed,
                        label = stringResource(R.string.feature_tailor_impl_provenance_confirmed),
                    )
                }
                if (fact.entryTitle.isNotEmpty()) {
                    Text(text = fact.entryTitle, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
                }
                HhEvidenceText(
                    text = buildAnnotatedString { withStyle(evidenceMarkSpanStyle()) { append(fact.text) } },
                    style = HhTheme.typography.bodyL,
                    color = HhTheme.colors.body,
                )
            }
        }
        HhOutlineButton(
            label = stringResource(R.string.feature_tailor_impl_source_sheet_close),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CoverLetterBottomBar(uiState: CoverLetterUiState, actions: CoverLetterActions) {
    HhBottomActionBar {
        when (uiState.stage) {
            CoverLetterStage.OFFER -> {
                HhOutlineButton(
                    label = stringResource(R.string.feature_tailor_impl_cover_letter_no_thanks),
                    onClick = actions.onSkipLetter,
                    modifier = Modifier.weight(1f),
                )
                HhPrimaryButton(
                    label = stringResource(R.string.feature_tailor_impl_cover_letter_write_one),
                    onClick = actions.onWriteOne,
                    modifier = Modifier.weight(1f),
                )
            }
            CoverLetterStage.GENERATING -> HhOutlineButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_leave),
                onClick = actions.onNavigateBack,
                modifier = Modifier.weight(1f),
            )
            CoverLetterStage.READY, CoverLetterStage.NO_MATCHING_EVIDENCE -> HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_preview_export),
                onClick = actions.onPreviewExport,
                trailingIcon = HhIcons.ArrowForward,
                modifier = Modifier.weight(1f),
            )
            CoverLetterStage.EMPTY_PROFILE -> HhOutlineButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_skip),
                onClick = actions.onSkipLetter,
                modifier = Modifier.weight(1f),
            )
            CoverLetterStage.ERROR -> {
                HhOutlineButton(
                    label = stringResource(R.string.feature_tailor_impl_cover_letter_skip),
                    onClick = actions.onSkipLetter,
                    modifier = Modifier.weight(1f),
                )
                HhPrimaryButton(
                    label = stringResource(R.string.feature_tailor_impl_cover_letter_retry),
                    onClick = actions.onRetry,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
