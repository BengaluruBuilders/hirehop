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
import com.tailormyresume.feature.profile.impl.ProfileExit
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
    private var returnsToProfile = false

    val uiState: StateFlow<EvidencePathUiState> = mutableState.asStateFlow()

    fun onEnter(key: FactEvidenceNavKey) {
        if (hasEntered) return
        hasEntered = true
        forcedOffline = key.scenario == DebugScenario.OFFLINE
        returnsToProfile = key.returnsToProfile
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
            EvidencePathAction.NextQuestion -> onNextQuestion()
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
                stamped = null,
                anchor = null,
                isDone = false,
                message = null,
            )
        }
    }

    private fun onNextQuestion() {
        mutableState.update { state -> if (state.stamped == null) state else state.advanced(skipNote = null) }
    }

    private fun onAnswerChanged(value: String) {
        mutableState.update { it.copy(answer = value, problem = null, message = null) }
    }

    private fun onSkip() {
        mutableState.update { state ->
            val category = state.category ?: return@update state
            if (state.stamped != null) state else state.advanced(skipNote = EvidenceSkipNote(category, state.questionNumber))
        }
    }

    private fun onSave() {
        val state = mutableState.value
        val category = state.category ?: return
        if (!state.canSave) return
        val anchor = state.anchor
        if (anchor != null) {
            attachToAnchor(state, category, anchor)
            return
        }
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
                    val filed = outcome.entries.map { EvidenceFactCard(category, it) }
                    current.copy(
                        cards = current.cards + filed,
                        isSaving = false,
                        answer = "",
                        stamped = filed.lastOrNull(),
                        anchor = filed.lastOrNull(),
                    )
                }
            } else {
                mutableState.update { it.copy(isSaving = false, message = EvidenceMessage.SAVE_FAILED) }
            }
        }
    }

    private fun attachToAnchor(state: EvidencePathUiState, category: EvidenceCategory, anchor: EvidenceFactCard) {
        mutableState.value = state.copy(isSaving = true, message = null)
        viewModelScope.launch {
            val updated = runCatching { addUserStatedFacts.attachBullet(anchor.entry.id, state.answer) }.getOrNull()
            if (updated == null) {
                mutableState.update { it.copy(isSaving = false, message = EvidenceMessage.SAVE_FAILED) }
                return@launch
            }
            val card = EvidenceFactCard(category, updated)
            mutableState.update { current ->
                current.copy(
                    cards = current.cards.map { if (it.entry.id == updated.id) card else it },
                    isSaving = false,
                    answer = "",
                    stamped = card,
                    anchor = card,
                )
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
                stamped = null,
                anchor = null,
                isDone = false,
            )
        }
    }

    private fun onFinish() {
        viewModelScope.launch {
            val exit = if (returnsToProfile) ProfileExit.Profile else exitResolver.resolve()
            mutableState.update { it.copy(navigation = EvidenceNavigation.Exit(exit)) }
        }
    }

    private fun EvidencePathUiState.advanced(skipNote: EvidenceSkipNote?): EvidencePathUiState {
        val current = category ?: return this
        if (questionIndex < current.questionCount - 1) {
            return copy(questionIndex = questionIndex + 1, answer = "", problem = null, skipNote = skipNote, stamped = null)
        }
        return copy(category = null, questionIndex = 0, answer = "", problem = null, skipNote = null, stamped = null, isDone = true)
    }
}
