package com.hirehop.feature.tailor.impl.coverletter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhBottomSheet
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhEvidenceText
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhMonogram
import com.hirehop.core.designsystem.component.HhPillRow
import com.hirehop.core.designsystem.component.HhPillRowStyle
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.HhToastHost
import com.hirehop.core.designsystem.component.evidenceMarkSpanStyle
import com.hirehop.core.designsystem.component.rememberHhToastState
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.tailor.impl.BannerTone
import com.hirehop.feature.tailor.impl.GenerationStep
import com.hirehop.feature.tailor.impl.GenerationSteps
import com.hirehop.feature.tailor.impl.NoteLine
import com.hirehop.feature.tailor.impl.NoticeStrip
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.StatusCard
import com.hirehop.feature.tailor.impl.StatusPill
import com.hirehop.feature.tailor.impl.StepMark

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
        header = { CoverLetterHeader(uiState, actions) },
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
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            coverLetterItems(uiState, actions, sourceOrdinal) { sourceOrdinal = it }
        }
    }
    val sourceParagraph = uiState.paragraphs.firstOrNull { it.ordinal == sourceOrdinal }
    if (sourceParagraph != null) {
        HhBottomSheet(
            onDismissRequest = { sourceOrdinal = null },
            title = stringResource(R.string.feature_tailor_impl_cover_letter_fact_sheet_title),
        ) {
            ParagraphSources(
                position = uiState.positionOf(sourceParagraph),
                paragraph = sourceParagraph,
                onClose = { sourceOrdinal = null },
            )
        }
    }
}

@Composable
private fun CoverLetterHeader(uiState: CoverLetterUiState, actions: CoverLetterActions) {
    HhInnerHeader(
        title = if (uiState.stage == CoverLetterStage.OFFER) "" else stringResource(R.string.feature_tailor_impl_cover_letter_title),
        subtitle = headerSubtitle(uiState),
        onBack = actions.onNavigateBack,
        backContentDescription = stringResource(R.string.feature_tailor_impl_cover_letter_back),
    )
}

@Composable
private fun headerSubtitle(uiState: CoverLetterUiState): String? = when {
    uiState.stage != CoverLetterStage.READY && uiState.stage != CoverLetterStage.NO_MATCHING_EVIDENCE -> null
    uiState.isEditing -> stringResource(R.string.feature_tailor_impl_cover_letter_editing_subtitle)
    uiState.isOffline -> null
    else -> {
        val letterParagraphs = uiState.paragraphs.count { !it.isGreeting }
        val words = pluralStringResource(
            R.plurals.feature_tailor_impl_cover_letter_words,
            uiState.wordCount,
            uiState.wordCount,
        )
        val paragraphs = pluralStringResource(
            R.plurals.feature_tailor_impl_cover_letter_paragraphs,
            letterParagraphs,
            letterParagraphs,
        )
        "$words · $paragraphs"
    }
}

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
        CoverLetterStage.OFFER -> item(key = "offer") { OfferCard(uiState, actions) }
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
            if (uiState.stage == CoverLetterStage.READY && !isWithinWordTarget(uiState.wordCount)) {
                item(key = "word-range") {
                    NoteLine(
                        text = pluralStringResource(
                            R.plurals.feature_tailor_impl_cover_letter_words_outside_range,
                            uiState.wordCount,
                            uiState.wordCount,
                        ),
                        icon = HhIcons.Info,
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
private fun OfferCard(uiState: CoverLetterUiState, actions: CoverLetterActions) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        HhHeadline(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_offer_title),
            style = HhTheme.typography.displayM,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_offer_body),
            style = HhTheme.typography.bodyL,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhCard {
            Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HhMonogram(text = uiState.jobCompany.ifBlank { uiState.jobTitle }, size = 44.dp)
                Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
                    Text(
                        text = uiState.jobTitle.trim().ifEmpty { stringResource(R.string.feature_tailor_impl_role_not_set) },
                        style = HhTheme.typography.titleM,
                        color = HhTheme.colors.onSurface,
                    )
                    Text(
                        text = uiState.jobCompany.trim().ifEmpty { stringResource(R.string.feature_tailor_impl_company_not_set) },
                        style = HhTheme.typography.bodyS,
                        color = HhTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
        uiState.exportedFileName?.let { fileName ->
            HhPillRow(
                title = fileName,
                onClick = {},
                style = HhPillRowStyle.Neutral,
                icon = HhIcons.Description,
                trailingIcon = null,
                titleMaxLines = 1,
                modifier = Modifier.clearAndSetSemantics { contentDescription = fileName },
            )
        }
        HhPillRow(
            title = stringResource(R.string.feature_tailor_impl_cover_letter_prep_title),
            subtitle = stringResource(R.string.feature_tailor_impl_cover_letter_prep_subtitle),
            onClick = actions.onPrepQuestions,
            style = HhPillRowStyle.Neutral,
            icon = HhIcons.Description,
        )
    }
}

@Composable
private fun GeneratingCard(uiState: CoverLetterUiState) {
    val steps = listOf(
        GenerationStep(
            title = stringResource(R.string.feature_tailor_impl_cover_letter_step_pick),
            detail = pluralStringResource(
                R.plurals.feature_tailor_impl_cover_letter_step_pick_detail,
                uiState.factCount,
                uiState.factCount,
            ),
            status = stringResource(R.string.feature_tailor_impl_status_done),
            mark = StepMark.Done,
        ),
        GenerationStep(
            title = stringResource(R.string.feature_tailor_impl_cover_letter_step_write),
            detail = null,
            status = stringResource(R.string.feature_tailor_impl_status_in_progress),
            mark = StepMark.Now,
        ),
        GenerationStep(
            title = stringResource(R.string.feature_tailor_impl_cover_letter_step_check),
            detail = null,
            status = stringResource(R.string.feature_tailor_impl_status_up_next),
            mark = StepMark.Waiting,
        ),
    )
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        GenerationSteps(steps)
        NoteLine(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_generating_notice),
            icon = HhIcons.Bell,
        )
    }
}

@Composable
private fun LetterCard(
    uiState: CoverLetterUiState,
    actions: CoverLetterActions,
    sourceOrdinal: Int?,
    onSource: (Int) -> Unit,
) {
    HhCard {
        uiState.paragraphs.forEach { paragraph ->
            if (paragraph.isGreeting) {
                Text(
                    text = paragraph.text,
                    style = HhTheme.typography.bodyM.copy(fontWeight = FontWeight.Bold),
                    color = HhTheme.colors.onSurface,
                )
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
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        if (editing) {
            EditingBlock(paragraph, position, uiState, actions, onSource)
        } else {
            HhEvidenceText(
                text = paragraph.annotated(marked, evidenceMarkSpanStyle(), HhTheme.colors.partialContainer),
                style = HhTheme.typography.bodyM,
            )
            ParagraphFactChipsFlow(paragraph.facts.map { fact -> fact.displayId }.distinct(), onSource)
            ParagraphNotes(paragraph)
            ParagraphActions(
                paragraph = paragraph,
                isReported = paragraph.ordinal.toString() in uiState.reportedIds,
                actions = actions,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ParagraphFactChipsFlow(factIds: List<String>, onClick: () -> Unit) {
    if (factIds.isEmpty()) return
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d4 + HhTheme.spacing.xxs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d4),
    ) {
        factIds.forEach { id ->
            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = HhTheme.spacing.touch)
                    .clickable(role = Role.Button, onClick = onClick),
                contentAlignment = Alignment.CenterStart,
            ) {
                HhFactId(id = id)
            }
        }
    }
}

private fun CoverLetterParagraph.annotated(marked: Boolean, style: SpanStyle, flagStyle: Color): AnnotatedString {
    val quote = flag?.quote
    val flagRange = quote?.let { text.indexOf(it).takeIf { index -> index >= 0 }?.let { index -> index until index + it.length } }
    if (flagRange != null && !isUserEdited) {
        return buildAnnotatedString {
            append(text)
            addStyle(SpanStyle(background = flagStyle), flagRange.first, flagRange.last + 1)
        }
    }
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
            tone = BannerTone.Warn,
        )
    }
    if (paragraph.isUserEdited) {
        StatusPill(
            label = stringResource(R.string.feature_tailor_impl_cover_letter_edited_by_you),
            icon = HhIcons.Edit,
            color = HhTheme.colors.onSurface,
        )
        NoteLine(
            text = stringResource(R.string.feature_tailor_impl_cover_letter_user_edited_note),
            icon = HhIcons.Info,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ParagraphActions(paragraph: CoverLetterParagraph, isReported: Boolean, actions: CoverLetterActions) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
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
private fun EditingBlock(
    paragraph: CoverLetterParagraph,
    position: Int,
    uiState: CoverLetterUiState,
    actions: CoverLetterActions,
    onSource: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhTextField(
            value = uiState.editingText,
            onValueChange = actions.onEditTextChanged,
            label = stringResource(R.string.feature_tailor_impl_cover_letter_edit_label, position),
            singleLine = false,
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = HhIcons.Verified,
                contentDescription = null,
                tint = HhTheme.colors.primary,
                modifier = Modifier.heightIn(min = HhTheme.spacing.d16),
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_cover_letter_still_backed),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.primary,
            )
        }
        ParagraphFactChipsFlow(paragraph.facts.map { fact -> fact.displayId }.distinct(), onSource)
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhSecondaryButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_edit_cancel),
                onClick = actions.onCancelEdit,
                modifier = Modifier.weight(1f),
            )
            HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_done_editing),
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
        paragraph.facts.forEach { fact ->
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HhFactId(id = fact.displayId)
                    HhProvenanceChip(
                        kind = HhProvenanceKind.Confirmed,
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
                Text(
                    text = stringResource(R.string.feature_tailor_impl_cover_letter_used_in_paragraph, position),
                    style = HhTheme.typography.labelM,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
        HhPrimaryButton(
            label = stringResource(R.string.feature_tailor_impl_cover_letter_done),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CoverLetterBottomBar(uiState: CoverLetterUiState, actions: CoverLetterActions) {
    when (uiState.stage) {
        CoverLetterStage.OFFER -> HhBottomActionBar(stacked = true) {
            HhSecondaryButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_no_thanks),
                onClick = actions.onSkipLetter,
                modifier = Modifier.fillMaxWidth(),
            )
            HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_write_one),
                onClick = actions.onWriteOne,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = HhIcons.Edit,
            )
        }
        CoverLetterStage.GENERATING -> HhBottomActionBar {
            HhSecondaryButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_leave),
                onClick = actions.onNavigateBack,
                modifier = Modifier.weight(1f),
            )
        }
        CoverLetterStage.READY, CoverLetterStage.NO_MATCHING_EVIDENCE -> HhBottomActionBar {
            HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_export_letter),
                onClick = actions.onPreviewExport,
                modifier = Modifier.weight(1f),
            )
        }
        CoverLetterStage.EMPTY_PROFILE -> HhBottomActionBar {
            HhSecondaryButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_skip),
                onClick = actions.onSkipLetter,
                modifier = Modifier.weight(1f),
            )
        }
        CoverLetterStage.ERROR -> HhBottomActionBar(stacked = true) {
            HhSecondaryButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_skip),
                onClick = actions.onSkipLetter,
                modifier = Modifier.fillMaxWidth(),
            )
            HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_cover_letter_retry),
                onClick = actions.onRetry,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
