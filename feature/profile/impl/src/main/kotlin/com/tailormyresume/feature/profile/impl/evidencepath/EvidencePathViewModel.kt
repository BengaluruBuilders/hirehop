package com.tailormyresume.feature.profile.impl.evidencepath

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.domain.AddUserStatedFactsUseCase
import com.tailormyresume.core.domain.fact.AddFactsOutcome
import com.tailormyresume.core.domain.fact.FactDraft
import com.tailormyresume.core.domain.fact.FactDraftValidator
import com.tailormyresume.core.domain.fact.FactField
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.feature.profile.api.navigation.FactEvidenceNavKey
import com.tailormyresume.feature.profile.impl.ProfileExitResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EvidencePathViewModel @Inject internal constructor(
    private val addUserStatedFacts: AddUserStatedFactsUseCase,
    private val exitResolver: ProfileExitResolver,
    private val connectivityMonitor: ConnectivityMonitor,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow(EvidencePathUiState())

    private var hasEntered = false
    private var forcedOffline = false

    val uiState: StateFlow<EvidencePathUiState> = mutableState.asStateFlow()

    fun onEnter(key: FactEvidenceNavKey) {
        if (hasEntered) return
        hasEntered = true
        forcedOffline = key.scenario == DebugScenario.OFFLINE
        mutableState.value = evidencePathStateFor(scenario = key.scenario, category = key.category)
        viewModelScope.launch {
            val order = evidenceCategoriesFor(sessionRepository.observeCareerStage().first())
            mutableState.update { it.copy(categoryOrder = order) }
        }
        connectivityMonitor.isOnline
            .onEach { online -> mutableState.update { it.copy(isOffline = forcedOffline || !online) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: EvidencePathAction) {
        when (action) {
            is EvidencePathAction.CategoryChosen -> onCategoryChosen(action.category)
            is EvidencePathAction.AnswerChanged -> onAnswerChanged(action.value)
            EvidencePathAction.Save -> onSave()
            EvidencePathAction.Skip -> onSkip()
            EvidencePathAction.NextQuestion -> Unit
            EvidencePathAction.AddMore -> onAddMore()
            EvidencePathAction.Finish -> onFinish()
            EvidencePathAction.NavigationConsumed -> mutableState.update { it.copy(navigation = null) }
            EvidencePathAction.DismissMessage -> mutableState.update { it.copy(message = null) }
        }
    }

    private fun onCategoryChosen(category: EvidenceCategory) {
        mutableState.update {
            it.copy(
                category = category,
                questionIndex = 0,
                answer = "",
                problem = null,
                skipNote = null,
                isDone = false,
                message = null,
            )
        }
    }

    private fun onAnswerChanged(value: String) {
        mutableState.update { it.copy(answer = value, problem = null, message = null) }
    }

    private fun onSkip() {
        mutableState.update { state ->
            val category = state.category ?: return@update state
            state.advanced(skipNote = EvidenceSkipNote(category, state.questionNumber))
        }
    }

    private fun onSave() {
        val state = mutableState.value
        val category = state.category ?: return
        if (!state.canSave) return
        val parts = splitAnswer(state.answer)
        val draft = FactDraft(
            category = category.entryCategory,
            title = parts.title,
            organization = "",
            startDate = "",
            endDate = "",
            detail = parts.detail,
        )
        val problem = FactDraftValidator.validate(draft).firstOrNull()?.let {
            if (it.field == FactField.DETAIL) EvidenceFieldProblem.TOO_LONG else EvidenceFieldProblem.REQUIRED
        }
        if (problem != null) {
            mutableState.value = state.copy(problem = problem)
            return
        }
        mutableState.value = state.copy(isSaving = true, message = null)
        viewModelScope.launch {
            val outcome = runCatching { addUserStatedFacts(listOf(draft)) }.getOrNull()
            if (outcome is AddFactsOutcome.Added) {
                mutableState.update { current ->
                    val cards = current.cards + outcome.entries.map { EvidenceFactCard(category, it) }
                    current.copy(cards = cards, isSaving = false).advanced(skipNote = null)
                }
            } else {
                mutableState.update { it.copy(isSaving = false, message = EvidenceMessage.SAVE_FAILED) }
            }
        }
    }

    private fun onAddMore() {
        mutableState.update {
            it.copy(
                category = null,
                questionIndex = 0,
                answer = "",
                problem = null,
                skipNote = null,
                visited = emptySet(),
                isDone = false,
            )
        }
    }

    private fun onFinish() {
        viewModelScope.launch {
            val exit = exitResolver.resolve()
            mutableState.update { it.copy(navigation = EvidenceNavigation.Exit(exit)) }
        }
    }

    private fun EvidencePathUiState.advanced(skipNote: EvidenceSkipNote?): EvidencePathUiState {
        val current = category ?: return this
        if (questionIndex < current.questionCount - 1) {
            return copy(questionIndex = questionIndex + 1, answer = "", problem = null, skipNote = skipNote)
        }
        val seen = visited + current
        val next = categoryOrder.firstOrNull { it !in seen }
        return if (next == null) {
            copy(
                category = null,
                questionIndex = 0,
                answer = "",
                problem = null,
                visited = seen,
                skipNote = null,
                isDone = true,
            )
        } else {
            copy(
                category = next,
                questionIndex = 0,
                answer = "",
                problem = null,
                visited = seen,
                skipNote = skipNote,
            )
        }
    }
}
