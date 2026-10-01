package com.hirehop.feature.profile.impl.evidencepath

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSegmentedCounter
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.FactSource
import com.hirehop.core.ui.FactIdTag
import com.hirehop.core.ui.FactProvenanceChip
import com.hirehop.feature.profile.impl.R

private val HH_TOUCH_TARGET: Dp = 48.dp

@Composable
internal fun EvidencePathScreen(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_profile_impl_evidence_path_title),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                navigationIconContentDescription = stringResource(
                    R.string.feature_profile_impl_evidence_path_navigation_back_description,
                ),
                onNavigationClick = actions.onBackPrompt,
            )
        },
        bottomBar = { EvidenceBottomBar(uiState = uiState, actions = actions) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.isLoading) {
                EvidenceLoading()
            } else {
                EvidenceContent(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun EvidenceLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        Text(
            text = stringResource(R.string.feature_profile_impl_evidence_path_loading),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun EvidenceContent(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_profile_impl_evidence_path_offline_message),
            visible = uiState.isOffline,
        )
        if (uiState.message == EvidenceMessage.LOAD_FAILED) {
            HhErrorCallout(
                title = stringResource(R.string.feature_profile_impl_evidence_path_load_failed),
                actionLabel = stringResource(R.string.feature_profile_impl_evidence_path_add_more),
                onAction = actions.onDismissMessage,
            )
        }
        if (uiState.isSaveRejected) {
            HhErrorCallout(
                title = stringResource(R.string.feature_profile_impl_evidence_path_save_rejected_title),
                supportingText = stringResource(
                    R.string.feature_profile_impl_evidence_path_save_rejected_body,
                ),
                actionLabel = stringResource(R.string.feature_profile_impl_evidence_path_add_more),
                onAction = actions.onDismissMessage,
            )
        }
        if (uiState.cards.isNotEmpty()) {
            EvidenceFactCards(cards = uiState.cards)
        }
        val proofMs = HhTheme.motion.proof
        AnimatedContent(
            targetState = uiState.stage,
            transitionSpec = {
                fadeIn(animationSpec = tween(proofMs)) togetherWith
                    fadeOut(animationSpec = tween(proofMs))
            },
            label = "evidenceStage",
        ) { stage ->
            when {
                stage.done != null -> EvidenceDoneCard(done = stage.done, actions = actions)
                stage.question != null -> EvidenceQuestionCard(question = stage.question, actions = actions)
                else -> EvidencePicker(actions = actions)
            }
        }
        EvidenceOwnWordsNote()
    }
}

@Composable
private fun EvidencePicker(actions: EvidencePathActions) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        Text(
            text = stringResource(R.string.feature_profile_impl_evidence_path_picker_title),
            style = HhTheme.typography.titleLarge,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_profile_impl_evidence_path_picker_body),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        EvidenceCategoryChips(onCategoryChosen = actions.onCategoryChosen)
        HhButton(
            onClick = { actions.onCategoryChosen(EvidenceCategory.PROJECTS) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_profile_impl_evidence_path_picker_start))
        }
        Text(
            text = stringResource(R.string.feature_profile_impl_evidence_path_picker_skip_note),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EvidenceCategoryChips(onCategoryChosen: (EvidenceCategory) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        EVIDENCE_CATEGORIES.forEach { category ->
            val label = stringResource(categoryLabelRes(category))
            HhOutlinedButton(
                onClick = { onCategoryChosen(category) },
                modifier = Modifier
                    .heightIn(min = HH_TOUCH_TARGET)
                    .clearAndSetSemantics {
                        contentDescription = label
                    },
            ) {
                Text(text = label, style = HhTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun EvidenceQuestionCard(
    question: EvidenceQuestion,
    actions: EvidencePathActions,
) {
    val counter = stringResource(
        R.string.feature_profile_impl_evidence_path_step_counter_description,
        question.promptIndex + 1,
        question.totalPrompts,
    )
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhSegmentedCounter(
            current = question.promptIndex + 1,
            total = question.totalPrompts,
            modifier = Modifier.clearAndSetSemantics { contentDescription = counter },
        )
        HhCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(categoryLabelRes(question.category)),
                style = HhTheme.typography.labelMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
            Text(
                text = stringResource(categoryQuestionRes(question.category)),
                style = HhTheme.typography.titleMedium,
                color = HhTheme.colors.onSurface,
            )
            HhTextField(
                value = question.answers[question.prompt].orEmpty(),
                onValueChange = { actions.onAnswerChanged(question.prompt, it) },
                label = stringResource(promptLabelRes(question.prompt, question.category)),
                placeholder = stringResource(R.string.feature_profile_impl_evidence_path_placeholder),
                errorText = question.problems[question.prompt]?.let { problemText(it) },
                supportingText = if (question.prompt == EvidencePrompt.TITLE) {
                    {
                        Text(
                            text = stringResource(exampleRes(question.category)),
                            style = HhTheme.typography.bodySmall,
                            color = HhTheme.colors.onSurfaceVariant,
                        )
                    }
                } else if (question.prompt == EvidencePrompt.ORGANIZATION) {
                    {
                        Text(
                            text = stringResource(
                                R.string.feature_profile_impl_evidence_path_organization_optional,
                            ),
                            style = HhTheme.typography.bodySmall,
                            color = HhTheme.colors.onSurfaceVariant,
                        )
                    }
                } else {
                    null
                },
                singleLine = false,
                minLines = 2,
                modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
            )
        }
        HhButton(
            onClick = actions.onSkipPrompt,
            modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_profile_impl_evidence_path_skip))
        }
    }
}

@Composable
private fun EvidenceFactCards(cards: List<EvidenceFactCard>) {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.feature_profile_impl_evidence_path_answer_stamp),
            style = HhTheme.typography.labelMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        cards.forEach { card ->
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(sectionLabelRes(card.entryCategory)),
                        style = HhTheme.typography.labelSmall,
                        color = HhTheme.colors.onSurfaceVariant,
                    )
                    FactProvenanceChip(source = FactSource.USER_STATED, isConfirmed = false)
                    card.entry?.let { FactIdTag(factId = it.id) }
                }
                Text(
                    text = card.line,
                    style = HhTheme.typography.bodyMedium,
                    color = HhTheme.colors.onSurface,
                )
            }
        }
    }
}

@Composable
private fun EvidenceDoneCard(
    done: EvidenceDone,
    actions: EvidencePathActions,
) {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        HhSpotIllustration(kind = HhSpotKind.Done)
        Text(
            text = if (done.addedCount == 0) {
                stringResource(R.string.feature_profile_impl_evidence_path_done_nothing_title)
            } else {
                pluralStringResource(
                    R.plurals.feature_profile_impl_evidence_path_done_title,
                    done.addedCount,
                    done.addedCount,
                )
            },
            style = HhTheme.typography.titleLarge,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = if (done.addedCount == 0) {
                stringResource(R.string.feature_profile_impl_evidence_path_done_nothing_body)
            } else {
                stringResource(R.string.feature_profile_impl_evidence_path_done_body)
            },
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        if (done.skippedCount > 0) {
            Text(
                text = pluralStringResource(
                    R.plurals.feature_profile_impl_evidence_path_done_skipped,
                    done.skippedCount,
                    done.skippedCount,
                ),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        HhOutlinedButton(
            onClick = actions.onAddMore,
            modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_profile_impl_evidence_path_add_more))
        }
        HhButton(
            onClick = actions.onGoToProfile,
            modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_profile_impl_evidence_path_go_to_profile))
        }
    }
}

@Composable
private fun EvidenceOwnWordsNote() {
    Text(
        text = stringResource(R.string.feature_profile_impl_evidence_path_own_words_note),
        style = HhTheme.typography.bodySmall,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun EvidenceBottomBar(
    uiState: EvidencePathUiState,
    actions: EvidencePathActions,
) {
    HhBottomActionBar(
        contentPadding = PaddingValues(
            start = HhTheme.spacing.d20,
            end = HhTheme.spacing.d20,
            top = HhTheme.spacing.md,
            bottom = HhTheme.spacing.md,
        ),
        actions = {
            if (uiState.isPicker) {
                HhOutlinedButton(
                    onClick = actions.onAddMore,
                    modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
                ) {
                    Text(text = stringResource(R.string.feature_profile_impl_evidence_path_add_more))
                }
            } else {
                HhOutlinedButton(
                    onClick = actions.onSkipCategory,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
                ) {
                    Text(text = stringResource(R.string.feature_profile_impl_evidence_path_skip))
                }
                if (uiState.question?.isLastPrompt == true) {
                    HhButton(
                        onClick = actions.onSave,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
                    ) {
                        Text(text = stringResource(R.string.feature_profile_impl_evidence_path_save))
                    }
                } else {
                    HhButton(
                        onClick = actions.onNextPrompt,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.feature_profile_impl_evidence_path_more_questions,
                            ),
                        )
                    }
                }
            }
        },
    )
}

private val EvidencePathUiState.stage: EvidenceStage
    get() = EvidenceStage(done = done, question = question)
