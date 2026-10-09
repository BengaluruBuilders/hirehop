package com.tailormyresume.feature.profile.impl.evidencepath

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrErrorCallout
import com.tailormyresume.core.designsystem.component.TmrFactId
import com.tailormyresume.core.designsystem.component.TmrFilterChip
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrLoadingWheel
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrProvenanceChip
import com.tailormyresume.core.designsystem.component.TmrProvenanceKind
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSectionLabel
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.component.TmrTextField
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.fact.FactLineRenderer
import com.tailormyresume.feature.profile.impl.R
import com.tailormyresume.feature.profile.impl.common.FactCard
import com.tailormyresume.feature.profile.impl.common.NeutralChip
import com.tailormyresume.feature.profile.impl.common.Note
import com.tailormyresume.feature.profile.impl.common.NoteTone
import com.tailormyresume.feature.profile.impl.common.kindRes
import com.tailormyresume.feature.profile.impl.common.status

private val RowGap = 10.dp
private val ChipGap = 6.dp
private val CategoryCardHeight = 112.dp
private val CategoryTile = 44.dp
private val DiscSize = 88.dp
private val ProgressHeight = 8.dp
private const val CATEGORY_COLUMNS = 2
private const val ANSWER_LINES = 5

@Composable
internal fun EvidencePathScreen(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TmrScreen(
        modifier = modifier,
        sheet = false,
        header = {
            TmrInnerHeader(
                title = stringResource(
                    uiState.category?.takeUnless { uiState.isDone }?.labelRes()
                        ?: R.string.feature_profile_impl_evidence_path_title,
                ),
                onBack = onBack,
                backContentDescription = stringResource(R.string.feature_profile_impl_evidence_path_back),
            )
        },
        bottomBar = evidenceBottomBar(uiState, actions),
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                TmrLoadingWheel(contentDesc = stringResource(R.string.feature_profile_impl_evidence_path_loading))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = TmrTheme.spacing.gutter),
                verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
            ) {
                Banners(uiState)
                when {
                    uiState.isDone -> DoneContent(uiState)
                    uiState.category == null -> PickerContent(uiState, actions)
                    else -> QuestionContent(uiState, actions)
                }
            }
        }
    }
}

@Composable
private fun Banners(uiState: EvidencePathUiState) {
    if (uiState.isOffline) {
        TmrOfflineBanner(message = stringResource(R.string.feature_profile_impl_evidence_path_offline_message))
    }
    when (uiState.message) {
        EvidenceMessage.LOAD_FAILED ->
            TmrErrorCallout(title = stringResource(R.string.feature_profile_impl_evidence_path_load_failed))
        EvidenceMessage.SAVE_FAILED ->
            TmrErrorCallout(title = stringResource(R.string.feature_profile_impl_evidence_path_save_failed))
        null -> Unit
    }
}

private fun evidenceBottomBar(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
): (@Composable () -> Unit)? = when {
    uiState.isLoading -> null
    uiState.isDone -> ({ DoneActionBar(actions) })
    uiState.category != null -> ({ QuestionActionBar(uiState, actions) })
    else -> null
}

@Composable
private fun QuestionActionBar(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
) {
    val stamped = uiState.stamped
    TmrBottomActionBar(stacked = true, primaryLast = false) {
        if (stamped != null) {
            TmrPrimaryButton(
                label = stringResource(R.string.feature_profile_impl_evidence_path_next_question),
                onClick = actions.onNextQuestion,
                modifier = Modifier.weight(1f),
            )
            TmrOutlineButton(
                label = stringResource(R.string.feature_profile_impl_evidence_path_edit),
                onClick = { actions.onEditFact(stamped.entry.id, stamped.entry.category.name) },
                modifier = Modifier.weight(1f),
            )
        } else {
            TmrPrimaryButton(
                label = stringResource(R.string.feature_profile_impl_evidence_path_save),
                onClick = actions.onSave,
                enabled = uiState.canSave,
                modifier = Modifier.weight(1f),
            )
            TmrTextButton(
                label = stringResource(R.string.feature_profile_impl_evidence_path_skip),
                onClick = actions.onSkip,
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DoneActionBar(actions: EvidencePathActions) {
    TmrBottomActionBar(stacked = true, primaryLast = false) {
        TmrPrimaryButton(
            label = stringResource(R.string.feature_profile_impl_evidence_path_back_to_profile),
            onClick = actions.onFinish,
            modifier = Modifier.weight(1f),
        )
        TmrTextButton(
            label = stringResource(R.string.feature_profile_impl_evidence_path_add_more),
            onClick = actions.onAddMore,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PickerContent(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
) {
    TmrHeadline(
        text = stringResource(R.string.feature_profile_impl_evidence_path_subtitle),
        style = TmrTheme.typography.headlineL,
    )
    Text(
        text = stringResource(R.string.feature_profile_impl_evidence_path_picker_title),
        style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
        color = TmrTheme.colors.onSurfaceVariant,
    )
    FlowRow(
        maxItemsInEachRow = CATEGORY_COLUMNS,
        horizontalArrangement = Arrangement.spacedBy(RowGap),
        verticalArrangement = Arrangement.spacedBy(RowGap),
    ) {
        uiState.categoryOrder.forEach { category ->
            CategoryCard(
                category = category,
                onClick = { actions.onCategoryChosen(category) },
                modifier = Modifier.weight(1f),
            )
        }
    }
    Note(
        text = stringResource(R.string.feature_profile_impl_evidence_path_picker_body),
        tone = NoteTone.Plain,
        icon = TmrIcons.Info,
    )
}

@Composable
private fun CategoryCard(
    category: EvidenceCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(category.labelRes())
    val hint = stringResource(category.hintRes())
    TmrCard(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = CategoryCardHeight)
            .semantics(mergeDescendants = true) { contentDescription = "$label. $hint" },
        contentPadding = PaddingValues(TmrTheme.spacing.md + TmrTheme.spacing.xxs),
    ) {
        Box(
            modifier = Modifier.size(CategoryTile).background(TmrTheme.colors.primaryContainer, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(category.icon(), contentDescription = null, tint = TmrTheme.colors.onSurface)
        }
        Text(text = label, style = TmrTheme.typography.titleS.copy(fontWeight = FontWeight.ExtraBold), color = TmrTheme.colors.onSurface)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionContent(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
) {
    val category = uiState.category ?: return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(ChipGap),
        verticalArrangement = Arrangement.spacedBy(ChipGap),
    ) {
        uiState.categoryOrder.forEach { item ->
            TmrFilterChip(
                label = stringResource(item.labelRes()),
                selected = item == category,
                onClick = { actions.onCategoryChosen(item) },
            )
        }
    }
    QuestionProgress(uiState = uiState)
    uiState.skipNote?.let { note -> SkippedRow(note) }
    val stamped = uiState.stamped
    if (stamped != null) {
        StampedCard(stamped, attached = uiState.questionIndex > 0)
    } else {
        QuestionCard(uiState = uiState, category = category)
        if (uiState.needsFirstAnswer) {
            Note(
                text = stringResource(R.string.feature_profile_impl_evidence_path_needs_first_answer),
                tone = NoteTone.Plain,
                icon = TmrIcons.Info,
            )
        }
        TextArea(uiState = uiState, actions = actions)
    }
}

@Composable
private fun SkippedRow(note: EvidenceSkipNote) {
    TmrCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.feature_profile_impl_evidence_path_skipped_row,
                    note.questionNumber,
                    stringResource(note.category.questionRes(note.questionNumber - 1)),
                ),
                modifier = Modifier.weight(1f),
                style = TmrTheme.typography.bodyM.copy(fontWeight = FontWeight.Bold),
                color = TmrTheme.colors.onSurface,
            )
            NeutralChip(
                label = stringResource(R.string.feature_profile_impl_evidence_path_skipped_chip),
                icon = TmrIcons.Info,
                tint = TmrTheme.colors.onSurfaceVariant,
            )
        }
    }
    Note(
        text = stringResource(R.string.feature_profile_impl_evidence_path_skipped_note),
        tone = NoteTone.Plain,
        icon = TmrIcons.Info,
    )
}

@Composable
private fun QuestionProgress(uiState: EvidencePathUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
        Text(
            text = uiState.projectName?.let { name ->
                stringResource(
                    R.string.feature_profile_impl_evidence_path_question_counter_named,
                    uiState.questionNumber,
                    uiState.questionTotal,
                    name,
                )
            } ?: stringResource(
                R.string.feature_profile_impl_evidence_path_question_counter,
                uiState.questionNumber,
                uiState.questionTotal,
            ),
            style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
            color = TmrTheme.colors.onSurface,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ProgressHeight)
                .background(TmrTheme.colors.primaryContainer, RoundedCornerShape(ProgressHeight / 2)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(uiState.questionNumber.toFloat() / uiState.questionTotal.coerceAtLeast(1))
                    .height(ProgressHeight)
                    .background(TmrTheme.colors.brand, RoundedCornerShape(ProgressHeight / 2)),
            )
        }
    }
}

@Composable
private fun StampedCard(card: EvidenceFactCard, attached: Boolean) {
    Note(
        text = if (attached) {
            stringResource(R.string.feature_profile_impl_evidence_path_stamped_banner_attached, card.entry.id, card.entry.title)
        } else {
            stringResource(R.string.feature_profile_impl_evidence_path_stamped_banner, card.entry.id)
        },
        tone = NoteTone.Positive,
        icon = TmrIcons.CheckCircle,
    )
    FactCard(
        id = card.entry.id,
        status = card.entry.status(),
        kind = stringResource(card.entry.category.kindRes()),
        summary = FactLineRenderer.render(card.entry),
        highlighted = true,
    )
}

@Composable
private fun QuestionCard(
    uiState: EvidencePathUiState,
    category: EvidenceCategory,
) {
    TmrCard {
        TmrSectionLabel(text = stringResource(R.string.feature_profile_impl_evidence_path_one_question))
        Text(
            text = stringResource(category.questionRes(uiState.questionIndex)),
            style = TmrTheme.typography.headlineM,
            color = TmrTheme.colors.onSurface,
        )
    }
}

@Composable
private fun TextArea(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
) {
    TmrTextField(
        value = uiState.answer,
        onValueChange = actions.onAnswerChanged,
        label = stringResource(R.string.feature_profile_impl_evidence_path_answer_label),
        placeholder = stringResource(R.string.feature_profile_impl_evidence_path_answer_placeholder),
        singleLine = false,
        minLines = ANSWER_LINES,
        errorText = uiState.problem?.let { stringResource(it.messageRes()) },
        supportingText = {
            Text(text = stringResource(R.string.feature_profile_impl_evidence_path_own_words_note))
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoneContent(uiState: EvidencePathUiState) {
    val count = uiState.cards.size
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier.size(DiscSize).background(TmrTheme.colors.metContainer, TmrTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(TmrIcons.CheckCircle, contentDescription = null, tint = TmrTheme.colors.met, modifier = Modifier.size(40.dp))
        }
        if (count == 0) {
            TmrHeadline(
                text = stringResource(R.string.feature_profile_impl_evidence_path_done_nothing_title),
                style = TmrTheme.typography.headlineL,
            )
            DoneBody(R.string.feature_profile_impl_evidence_path_done_nothing_body)
        } else {
            TmrHeadline(
                text = pluralStringResource(R.plurals.feature_profile_impl_evidence_path_done_title, count, count),
                style = TmrTheme.typography.headlineL,
            )
            DoneBody(R.string.feature_profile_impl_evidence_path_done_body)
        }
    }
    uiState.cards.forEach { card -> DoneRow(card) }
}

@Composable
private fun DoneBody(@StringRes text: Int) {
    Text(
        text = stringResource(text),
        style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
        color = TmrTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoneRow(card: EvidenceFactCard) {
    TmrCard {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
        ) {
            TmrFactId(id = card.entry.id)
            TmrProvenanceChip(
                kind = TmrProvenanceKind.UserStated,
                label = stringResource(R.string.feature_profile_impl_status_user_stated),
            )
        }
        Text(
            text = FactLineRenderer.render(card.entry),
            style = TmrTheme.typography.bodyM.copy(fontWeight = FontWeight.Bold),
            color = TmrTheme.colors.onSurface,
        )
    }
}
