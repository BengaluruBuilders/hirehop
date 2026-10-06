package com.hirehop.feature.profile.impl.evidencepath

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhFilterChip
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPillRow
import com.hirehop.core.designsystem.component.HhPillRowStyle
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.fact.FactLineRenderer
import com.hirehop.feature.profile.impl.R
import com.hirehop.feature.profile.impl.common.FactCard
import com.hirehop.feature.profile.impl.common.Note

private val RowGap = 10.dp
private val ChipGap = 6.dp
private val CategoryPillStyles =
    listOf(HhPillRowStyle.Coral, HhPillRowStyle.Jade, HhPillRowStyle.Marigold)
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
                title = stringResource(R.string.feature_profile_impl_evidence_path_title),
                subtitle = stringResource(R.string.feature_profile_impl_evidence_path_subtitle),
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
                verticalArrangement = Arrangement.spacedBy(RowGap),
            ) {
                Banners(uiState)
                when {
                    uiState.isDone -> DoneContent(uiState)
                    uiState.category == null -> PickerContent(actions)
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
    HhBottomActionBar {
        HhOutlineButton(
            label = stringResource(R.string.feature_profile_impl_evidence_path_skip),
            onClick = actions.onSkip,
            enabled = !uiState.isSaving,
            modifier = Modifier.weight(1f),
        )
        HhPrimaryButton(
            label = stringResource(R.string.feature_profile_impl_evidence_path_save),
            onClick = actions.onSave,
            enabled = uiState.canSave,
            trailingIcon = HhIcons.Check,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DoneActionBar(actions: EvidencePathActions) {
    HhBottomActionBar {
        HhOutlineButton(
            label = stringResource(R.string.feature_profile_impl_evidence_path_add_more),
            onClick = actions.onAddMore,
            modifier = Modifier.weight(1f),
        )
        HhPrimaryButton(
            label = stringResource(R.string.feature_profile_impl_evidence_path_back_to_profile),
            onClick = actions.onFinish,
            leadingIcon = HhIcons.Profile,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PickerContent(actions: EvidencePathActions) {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.lg)) {
        Column(verticalArrangement = Arrangement.spacedBy(ChipGap)) {
            Text(
                text = stringResource(R.string.feature_profile_impl_evidence_path_picker_title),
                style = HhTheme.typography.titleL,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_profile_impl_evidence_path_picker_body),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
        }
    }
    EVIDENCE_CATEGORIES.forEachIndexed { index, category ->
        CategoryRow(
            category = category,
            style = CategoryPillStyles[index % CategoryPillStyles.size],
            onClick = { actions.onCategoryChosen(category) },
        )
    }
}

@Composable
private fun CategoryRow(
    category: EvidenceCategory,
    style: HhPillRowStyle,
    onClick: () -> Unit,
) {
    val label = stringResource(category.labelRes())
    val hint = stringResource(category.hintRes())
    HhPillRow(
        title = label,
        onClick = onClick,
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "$label. $hint" },
        style = style,
        subtitle = hint,
        icon = category.icon(),
        trailingIcon = HhIcons.ArrowForward,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionContent(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
) {
    val category = uiState.category ?: return
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.sm + HhTheme.spacing.xxs)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(ChipGap),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            EVIDENCE_CATEGORIES.forEach { item ->
                HhFilterChip(
                    label = stringResource(item.labelRes()),
                    selected = item == category,
                    onClick = { actions.onCategoryChosen(item) },
                )
            }
        }
    }
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
    QuestionCard(uiState = uiState, category = category, actions = actions)
}

@Composable
private fun SavedCards(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
) {
    val cards = uiState.categoryCards
    if (cards.isEmpty()) return
    Text(
        text = stringResource(R.string.feature_profile_impl_evidence_path_saved_caption),
        style = HhTheme.typography.labelM,
        color = HhTheme.colors.onSurfaceVariant,
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
    actions: EvidencePathActions,
) {
    val categoryLabel = stringResource(category.labelRes())
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.lg)) {
        Column(verticalArrangement = Arrangement.spacedBy(RowGap)) {
            Text(
                text = stringResource(
                    R.string.feature_profile_impl_evidence_path_question_counter,
                    categoryLabel,
                    uiState.questionNumber,
                    uiState.questionTotal,
                ),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
            Text(
                text = stringResource(category.questionRes(uiState.questionIndex)),
                style = HhTheme.typography.titleL,
                color = HhTheme.colors.onSurface,
            )
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
    }
}

@Composable
private fun DoneContent(uiState: EvidencePathUiState) {
    val count = uiState.cards.size
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.lg + HhTheme.spacing.xxs)) {
        Column(verticalArrangement = Arrangement.spacedBy(ChipGap)) {
            if (count == 0) {
                Text(
                    text = stringResource(R.string.feature_profile_impl_evidence_path_done_nothing_title),
                    style = HhTheme.typography.titleL,
                    color = HhTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.feature_profile_impl_evidence_path_done_nothing_body),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.body,
                )
            } else {
                val title = pluralStringResource(
                    R.plurals.feature_profile_impl_evidence_path_done_title,
                    count,
                    count,
                )
                Row(
                    modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = title },
                    horizontalArrangement = Arrangement.spacedBy(RowGap),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = count.toString(),
                        style = HhTheme.typography.numeralHero,
                        color = HhTheme.colors.onSurface,
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.feature_profile_impl_evidence_path_done_caption,
                            count,
                        ),
                        style = HhTheme.typography.titleM,
                        color = HhTheme.colors.onSurface,
                        modifier = Modifier.padding(bottom = HhTheme.spacing.xs),
                    )
                }
                Text(
                    text = stringResource(R.string.feature_profile_impl_evidence_path_done_body),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.body,
                )
                uiState.cards.forEach { card -> DoneRow(card) }
            }
        }
    }
}

@Composable
private fun DoneRow(card: EvidenceFactCard) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = HhTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhFactId(id = card.entry.id)
            HhProvenanceChip(
                kind = HhProvenanceKind.UserStated,
                label = stringResource(R.string.feature_profile_impl_status_user_stated),
            )
        }
        Text(
            text = FactLineRenderer.render(card.entry),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurface,
        )
    }
}
