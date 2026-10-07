package com.hirehop.feature.profile.impl.evidencepath

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhFilterChip
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSectionLabel
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.fact.FactLineRenderer
import com.hirehop.feature.profile.impl.R
import com.hirehop.feature.profile.impl.common.FactCard
import com.hirehop.feature.profile.impl.common.Note
import com.hirehop.feature.profile.impl.common.NoteTone

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
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
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
                HhLoadingWheel(contentDesc = stringResource(R.string.feature_profile_impl_evidence_path_loading))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = HhTheme.spacing.gutter),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
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
        HhOfflineBanner(message = stringResource(R.string.feature_profile_impl_evidence_path_offline_message))
    }
    when (uiState.message) {
        EvidenceMessage.LOAD_FAILED ->
            HhErrorCallout(title = stringResource(R.string.feature_profile_impl_evidence_path_load_failed))
        EvidenceMessage.SAVE_FAILED ->
            HhErrorCallout(title = stringResource(R.string.feature_profile_impl_evidence_path_save_failed))
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
    HhBottomActionBar(stacked = true, primaryLast = false) {
        HhPrimaryButton(
            label = stringResource(R.string.feature_profile_impl_evidence_path_save),
            onClick = actions.onSave,
            enabled = uiState.canSave,
            modifier = Modifier.weight(1f),
        )
        HhTextButton(
            label = stringResource(R.string.feature_profile_impl_evidence_path_skip),
            onClick = actions.onSkip,
            enabled = !uiState.isSaving,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DoneActionBar(actions: EvidencePathActions) {
    HhBottomActionBar(stacked = true, primaryLast = false) {
        HhPrimaryButton(
            label = stringResource(R.string.feature_profile_impl_evidence_path_back_to_profile),
            onClick = actions.onFinish,
            modifier = Modifier.weight(1f),
        )
        HhTextButton(
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
    HhHeadline(
        text = stringResource(R.string.feature_profile_impl_evidence_path_subtitle),
        style = HhTheme.typography.headlineL,
    )
    Text(
        text = stringResource(R.string.feature_profile_impl_evidence_path_picker_title),
        style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
        color = HhTheme.colors.onSurfaceVariant,
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
        icon = HhIcons.Info,
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
    HhCard(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = CategoryCardHeight)
            .semantics(mergeDescendants = true) { contentDescription = "$label. $hint" },
        contentPadding = PaddingValues(HhTheme.spacing.md + HhTheme.spacing.xxs),
    ) {
        Box(
            modifier = Modifier.size(CategoryTile).background(HhTheme.colors.primaryContainer, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(category.icon(), contentDescription = null, tint = HhTheme.colors.onSurface)
        }
        Text(text = label, style = HhTheme.typography.titleS.copy(fontWeight = FontWeight.ExtraBold), color = HhTheme.colors.onSurface)
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
            HhFilterChip(
                label = stringResource(item.labelRes()),
                selected = item == category,
                onClick = { actions.onCategoryChosen(item) },
            )
        }
    }
    QuestionProgress(uiState = uiState)
    uiState.skipNote?.let { note ->
        Note(
            text = stringResource(
                R.string.feature_profile_impl_evidence_path_skipped_note,
                stringResource(note.category.labelRes()),
                note.questionNumber,
            ),
        )
    }
    SavedCards(uiState = uiState, actions = actions)
    QuestionCard(uiState = uiState, category = category)
    TextArea(uiState = uiState, actions = actions)
}

@Composable
private fun QuestionProgress(uiState: EvidencePathUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(
            text = stringResource(
                R.string.feature_profile_impl_evidence_path_question_counter,
                uiState.questionNumber,
                uiState.questionTotal,
            ),
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
            color = HhTheme.colors.onSurface,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ProgressHeight)
                .background(HhTheme.colors.primaryContainer, RoundedCornerShape(ProgressHeight / 2)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(uiState.questionNumber.toFloat() / uiState.questionTotal.coerceAtLeast(1))
                    .height(ProgressHeight)
                    .background(HhTheme.colors.brand, RoundedCornerShape(ProgressHeight / 2)),
            )
        }
    }
}

@Composable
private fun SavedCards(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
) {
    val cards = uiState.categoryCards
    if (cards.isEmpty()) return
    Note(
        text = stringResource(R.string.feature_profile_impl_evidence_path_saved_caption),
        tone = NoteTone.Positive,
        icon = HhIcons.CheckCircle,
    )
    cards.forEachIndexed { index, card ->
        FactCard(
            entry = card.entry,
            highlighted = index == cards.lastIndex,
            onEdit = { actions.onEditFact(card.entry.id, card.entry.category.name) },
        )
    }
}

@Composable
private fun QuestionCard(
    uiState: EvidencePathUiState,
    category: EvidenceCategory,
) {
    HhCard {
        HhSectionLabel(text = stringResource(R.string.feature_profile_impl_evidence_path_one_question))
        Text(
            text = stringResource(category.questionRes(uiState.questionIndex)),
            style = HhTheme.typography.headlineM,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun TextArea(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
) {
    HhTextField(
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
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier.size(DiscSize).background(HhTheme.colors.metContainer, HhTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(HhIcons.CheckCircle, contentDescription = null, tint = HhTheme.colors.met, modifier = Modifier.size(40.dp))
        }
        if (count == 0) {
            HhHeadline(
                text = stringResource(R.string.feature_profile_impl_evidence_path_done_nothing_title),
                style = HhTheme.typography.headlineL,
            )
            DoneBody(R.string.feature_profile_impl_evidence_path_done_nothing_body)
        } else {
            HhHeadline(
                text = pluralStringResource(R.plurals.feature_profile_impl_evidence_path_done_title, count, count),
                style = HhTheme.typography.headlineL,
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
        style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
        color = HhTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoneRow(card: EvidenceFactCard) {
    HhCard {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            HhFactId(id = card.entry.id)
            HhProvenanceChip(
                kind = HhProvenanceKind.UserStated,
                label = stringResource(R.string.feature_profile_impl_status_user_stated),
            )
        }
        Text(
            text = FactLineRenderer.render(card.entry),
            style = HhTheme.typography.bodyM.copy(fontWeight = FontWeight.Bold),
            color = HhTheme.colors.onSurface,
        )
    }
}
