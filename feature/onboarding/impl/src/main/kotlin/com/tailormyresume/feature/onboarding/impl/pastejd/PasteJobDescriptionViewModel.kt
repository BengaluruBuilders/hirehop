package com.tailormyresume.feature.onboarding.impl.pastejd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.common.network.Dispatcher
import com.tailormyresume.core.common.network.TmrDispatchers
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.data.repository.UsageAllowance
import com.tailormyresume.core.domain.AiException
import com.tailormyresume.core.domain.DiscardJobDraftsUseCase
import com.tailormyresume.core.domain.JobLabelProposal
import com.tailormyresume.core.domain.ProposeJobLabelUseCase
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.KeptJobDescription
import com.tailormyresume.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import com.tailormyresume.feature.onboarding.impl.common.observeOffline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PasteJobDescriptionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val nextOnboardingStep: NextOnboardingStepUseCase,
    private val connectivityMonitor: ConnectivityMonitor,
    private val usageAllowance: UsageAllowance,
    private val proposeJobLabel: ProposeJobLabelUseCase,
    private val discardJobDrafts: DiscardJobDraftsUseCase,
    @param:Dispatcher(TmrDispatchers.Default) private val computeDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val mutableState = MutableStateFlow(PasteJobDescriptionUiState())

    private var hasEntered = false

    private var isSubmitting = false

    private var proposedCompany = ""

    private var proposedRole = ""

    private var prefillJob: Job? = null

    val uiState: StateFlow<PasteJobDescriptionUiState> = mutableState.asStateFlow()

    fun onEnter(
        key: PasteJobDescriptionNavKey,
        sharedText: String = "",
    ) {
        if (hasEntered) return
        hasEntered = true
        mutableState.value = pasteJobDescriptionStateFor(
            scenario = key.scenario,
            sharedText = sharedText,
        )
        schedulePrefill()
        val forcedOffline = key.scenario == DebugScenario.OFFLINE
        viewModelScope.launch {
            connectivityMonitor.observeOffline(forcedOffline).collect { offline ->
                mutableState.update { it.copy(isOffline = offline) }
            }
        }
        if (key.scenario != DebugScenario.PENDING) {
            viewModelScope.launch {
                usageAllowance.observeAnalysesLeft().collect { left ->
                    mutableState.update { it.copy(freeAnalysesLeft = left) }
                }
            }
        }
    }

    fun onAction(action: PasteJobDescriptionAction) {
        when (action) {
            is PasteJobDescriptionAction.TextChanged -> onTextChanged(action.value)
            is PasteJobDescriptionAction.Pasted -> onPasted(action.value)
            is PasteJobDescriptionAction.CompanyChanged -> mutableState.update { it.copy(company = action.value) }
            is PasteJobDescriptionAction.RoleChanged -> mutableState.update { it.copy(role = action.value) }
            PasteJobDescriptionAction.ClearTapped -> onClear()
            PasteJobDescriptionAction.AnalyseTapped -> onAnalyse()
            PasteJobDescriptionAction.RetryTapped -> mutableState.update { it.copy(message = null) }
            PasteJobDescriptionAction.DismissMessageTapped -> mutableState.update { it.copy(message = null) }
            PasteJobDescriptionAction.NextStepConsumed -> mutableState.update { it.copy(nextStep = null) }
        }
    }

    private fun onTextChanged(value: String) {
        mutableState.update { state -> state.copy(text = value, message = null, nextStep = null) }
        schedulePrefill()
    }

    private fun schedulePrefill() {
        prefillJob?.cancel()
        val text = mutableState.value.text
        if (text.isBlank()) return
        prefillJob = viewModelScope.launch {
            delay(PREFILL_DEBOUNCE_MS)
            val proposal = try {
                withContext(computeDispatcher) { proposeJobLabel(text) }
            } catch (_: AiException) {
                return@launch
            }
            mutableState.update { it.withProposedLabels(proposal) }
        }
    }

    private fun PasteJobDescriptionUiState.withProposedLabels(proposal: JobLabelProposal): PasteJobDescriptionUiState {
        val fillsCompany = company.isBlank() || company == proposedCompany
        val fillsRole = role.isBlank() || role == proposedRole
        if (fillsCompany) proposedCompany = proposal.company
        if (fillsRole) proposedRole = proposal.role
        return copy(
            company = if (fillsCompany) proposal.company else company,
            role = if (fillsRole) proposal.role else role,
        )
    }

    private fun onPasted(value: String) {
        if (value.isBlank()) {
            mutableState.update { it.copy(message = PasteJobDescriptionMessage.NOTHING_TO_READ) }
        } else {
            onTextChanged(value)
        }
    }

    private fun onClear() {
        prefillJob?.cancel()
        mutableState.update { state ->
            if (state.canClear) {
                proposedCompany = ""
                proposedRole = ""
                state.copy(text = "", company = "", role = "", message = null, nextStep = null)
            } else {
                state
            }
        }
    }

    private fun onAnalyse() {
        val state = mutableState.value
        if (!state.canAnalyse || isSubmitting) return
        isSubmitting = true
        viewModelScope.launch {
            if (usageAllowance.observeAnalysesLeft().first() <= 0) {
                isSubmitting = false
                return@launch
            }
            val kept = KeptJobDescription(
                text = state.text.trim(),
                company = state.company.trim(),
                role = state.role.trim(),
            )
            val previous = sessionRepository.observeKeptJobDescription().first()
            if (previous != null && previous.draftKey != kept.draftKey) discardJobDrafts(previous)
            sessionRepository.keepJobDescription(kept)
            val step = nextOnboardingStep()
            isSubmitting = false
            mutableState.update { it.copy(nextStep = step) }
        }
    }

    private companion object {
        const val PREFILL_DEBOUNCE_MS = 300L
    }
}
