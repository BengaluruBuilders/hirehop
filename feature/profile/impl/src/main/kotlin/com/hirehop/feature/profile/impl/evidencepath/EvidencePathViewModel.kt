package com.hirehop.feature.profile.impl.evidencepath

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.domain.AddUserStatedFactsUseCase
import com.hirehop.core.domain.fact.AddFactsOutcome
import com.hirehop.core.domain.fact.FactDraft
import com.hirehop.core.domain.fact.FactDraftError
import com.hirehop.core.domain.fact.FactDraftErrorReason
import com.hirehop.core.domain.fact.FactDraftValidator
import com.hirehop.core.domain.fact.FactField
import com.hirehop.core.domain.fact.FactLineRenderer
import com.hirehop.core.model.ProfileEntry
import com.hirehop.feature.profile.api.navigation.FactEvidenceNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EvidencePathViewModel @Inject constructor(
    private val addUserStatedFacts: AddUserStatedFactsUseCase,
) : ViewModel() {

    private val mutableState = MutableStateFlow(EvidencePathUiState())

    private var hasEntered = false

    val uiState: StateFlow<EvidencePathUiState> = mutableState.asStateFlow()

    fun onEnter(key: FactEvidenceNavKey) {
        if (hasEntered) return
        hasEntered = true
        mutableState.value = evidencePathStateFor(scenario = key.scenario, category = key.category)
    }

    fun onAction(action: EvidencePathAction) {
        when (action) {
            is EvidencePathAction.CategoryChosen -> onCategoryChosen(action.category)
            is EvidencePathAction.AnswerChanged -> onAnswerChanged(action.prompt, action.value)
            EvidencePathAction.NextPrompt -> onNextPrompt()
            EvidencePathAction.BackPrompt -> onBackPrompt()
            EvidencePathAction.SkipPrompt -> onSkipPrompt()
            EvidencePathAction.Save -> onSave()
            EvidencePathAction.SkipCategory -> onSkipCategory()
            EvidencePathAction.AddMore -> onAddMore()
            EvidencePathAction.Finish -> onFinish()
            EvidencePathAction.DismissMessage -> onDismissMessage()
        }
    }

    private fun onCategoryChosen(category: EvidenceCategory) {
        mutableState.update { state ->
            state.copy(
                category = category,
                question = category.firstQuestion(),
                isSaveRejected = false,
                message = null,
            )
        }
    }

    private fun onAnswerChanged(
        prompt: EvidencePrompt,
        value: String,
    ) {
        mutableState.update { state ->
            val question = state.question ?: return@update state
            state.copy(
                question = question.copy(
                    answers = question.answers + (prompt to value),
                    problems = question.problems - prompt,
                ),
                isSaveRejected = false,
                message = null,
            )
        }
    }

    private fun onNextPrompt() {
        mutableState.update { state ->
            val question = state.question ?: return@update state
            if (question.isLastPrompt) return@update state
            state.copy(question = question.copy(promptIndex = question.promptIndex + 1))
        }
    }

    private fun onBackPrompt() {
        mutableState.update { state ->
            val question = state.question ?: return@update state
            when {
                question.promptIndex > 0 -> state.copy(question = question.copy(promptIndex = question.promptIndex - 1))
                else -> state.copy(category = null, question = null, cards = state.cards, message = null)
            }
        }
    }

    private fun onSkipPrompt() {
        mutableState.update { state ->
            val question = state.question ?: return@update state
            if (question.isLastPrompt) {
                return@update state.advancePast(from = question.category, state.skippedCategory(), true)
            }
            state.copy(
                question = question.copy(promptIndex = question.promptIndex + 1),
            )
        }
    }

    private fun onSkipCategory() {
        mutableState.update { state ->
            state.advancePast(from = state.category, state.skippedCategory(), true)
        }
    }

    private fun onSave() {
        val state = mutableState.value
        val question = state.question ?: return
        if (state.isSaving) return
        val draft = question.toDraft()
        val problems = problemsOf(question, FactDraftValidator.validate(draft))
        if (problems.isNotEmpty()) {
            mutableState.value = state.copy(
                question = question.copy(problems = problems),
                isSaveRejected = true,
                message = EvidenceMessage.SAVE_REJECTED,
            )
            return
        }
        val card = question.toCard()
        mutableState.value = state.copy(
            isSaving = true,
            cards = state.cards.withoutCategory(question.category) + card,
            question = null,
            category = null,
            isSaveRejected = false,
            message = null,
        )
        viewModelScope.launch {
            when (val outcome = addUserStatedFacts(listOf(draft))) {
                is AddFactsOutcome.Added -> onAdded(question.category, outcome.entries)
                is AddFactsOutcome.Rejected -> onRejected(question, outcome.errors)
                AddFactsOutcome.NothingToAdd -> onAdded(question.category, emptyList())
            }
        }
    }

    private fun onAdded(
        saved: EvidenceCategory,
        entries: List<ProfileEntry>,
    ) {
        mutableState.update { state ->
            val stamped = state.withEntry(entries)
            val next = stamped.advancePast(from = saved, null, false)
            val added = next.addedCount + entries.size
            next.copy(
                isSaving = false,
                cards = stamped.cards,
                addedCount = added,
                done = if (next.category == null) EvidenceDone(added, next.skipped.size) else next.done,
                message = if (state.isOffline) EvidenceMessage.OFFLINE_QUEUED else EvidenceMessage.SAVED,
            )
        }
    }

    private fun onRejected(
        question: EvidenceQuestion,
        errors: List<FactDraftError>,
    ) {
        mutableState.update { state ->
            state.copy(
                isSaving = false,
                isSaveRejected = true,
                category = question.category,
                question = question.copy(problems = problemsOf(question, errors)),
                message = EvidenceMessage.SAVE_REJECTED,
            )
        }
    }

    private fun onAddMore() {
        mutableState.update { state ->
            EvidencePathUiState(
                isOffline = state.isOffline,
                startCategory = state.startCategory,
                cards = state.cards,
                skipped = state.skipped,
            )
        }
    }

    private fun onFinish() {
        mutableState.update { it.copy(done = null, message = null) }
    }

    private fun onDismissMessage() {
        mutableState.update { it.copy(message = null, isSaveRejected = false) }
    }

    private fun EvidencePathUiState.skippedCategory(): List<EvidenceCategory> {
        val current = category ?: return skipped
        return if (current in skipped) skipped else skipped + current
    }

    private fun EvidencePathUiState.advancePast(
        from: EvidenceCategory?,
        skippedCategories: List<EvidenceCategory>?,
        showDoneWhenExhausted: Boolean,
    ): EvidencePathUiState {
        val nextCategory = EVIDENCE_CATEGORIES
            .dropWhile { it != from }
            .drop(1)
            .firstOrNull()
        val advanced = EvidencePathUiState(
            isOffline = isOffline,
            isSaving = isSaving,
            startCategory = startCategory,
            category = nextCategory,
            question = nextCategory?.firstQuestion(),
            cards = cards,
            skipped = skippedCategories ?: skipped,
            addedCount = addedCount,
            done = done,
        )
        return if (showDoneWhenExhausted && nextCategory == null) {
            advanced.copy(done = EvidenceDone(advanced.addedCount, advanced.skipped.size))
        } else {
            advanced
        }
    }

    private fun EvidenceCategory.firstQuestion(): EvidenceQuestion = EvidenceQuestion(
        category = this,
        promptIndex = 0,
        totalPrompts = prompts().size,
        answers = emptyMap(),
    )

    private fun EvidenceQuestion.toDraft(): FactDraft = FactDraft(
        category = category.entryCategory,
        title = title.trim(),
        organization = organization.trim(),
        startDate = "",
        endDate = "",
        detail = detail.trim(),
    )

    private fun EvidenceQuestion.toCard(): EvidenceFactCard = EvidenceFactCard(
        category = category,
        entryCategory = category.entryCategory,
        line = FactLineRenderer.render(toDraft()),
        answer = listOf(title, detail, organization)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(" "),
    )

    private fun problemsOf(
        question: EvidenceQuestion,
        errors: List<FactDraftError>,
    ): Map<EvidencePrompt, EvidenceFieldProblem> = errors
        .mapNotNull { error ->
            error.toPrompt()?.let { prompt -> prompt to error.reason.toProblem() }
        }
        .toMap()

    private fun EvidencePathUiState.withEntry(
        entries: List<ProfileEntry>,
    ): EvidencePathUiState {
        val entry = entries.firstOrNull() ?: return this
        if (cards.none { it.entryCategory == entry.category }) return this
        return copy(
            cards = cards.map { card ->
                if (card.entryCategory == entry.category) {
                    card.copy(entry = entry, line = FactLineRenderer.render(entry))
                } else {
                    card
                }
            },
        )
    }

    private fun List<EvidenceFactCard>.withoutCategory(
        category: EvidenceCategory,
    ): List<EvidenceFactCard> = filterNot { it.category == category }

    private fun FactDraftError.toPrompt(): EvidencePrompt? = when (field) {
        FactField.TITLE -> EvidencePrompt.TITLE
        FactField.DETAIL -> EvidencePrompt.DETAIL
        FactField.ORGANIZATION -> EvidencePrompt.ORGANIZATION
        FactField.START_DATE, FactField.END_DATE -> null
    }

    private fun FactDraftErrorReason.toProblem(): EvidenceFieldProblem = when (this) {
        FactDraftErrorReason.REQUIRED -> EvidenceFieldProblem.REQUIRED
        FactDraftErrorReason.END_BEFORE_START -> EvidenceFieldProblem.END_BEFORE_START
        FactDraftErrorReason.TOO_LONG -> EvidenceFieldProblem.TOO_LONG
    }
}
