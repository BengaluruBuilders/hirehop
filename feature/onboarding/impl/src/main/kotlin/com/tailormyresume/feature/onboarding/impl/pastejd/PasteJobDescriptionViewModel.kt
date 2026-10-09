package com.tailormyresume.feature.onboarding.impl.pastejd

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("VisibleForTests")
@HiltViewModel
class PasteJobDescriptionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val nextOnboardingStep: NextOnboardingStepUseCase,
    private val connectivityMonitor: ConnectivityMonitor,
    private val usageAllowance: UsageAllowance,
    private val proposeJobLabel: ProposeJobLabelUseCase,
    private val discardJobDrafts: DiscardJobDraftsUseCase,
    @param:Dispatcher(TmrDispatchers.Default) private val computeDispatcher: CoroutineDispatcher,
    private val savedState: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    private val mutableState = MutableStateFlow(PasteJobDescriptionUiState())

    private var hasEntered = false

    private var isSubmitting = false

    private var proposedCompany = ""

    private var proposedRole = ""

    private var labelsEditedSinceText = false

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
        restoreTypedInput()
        schedulePrefill()
        mutableState
            .map { listOf(it.text.take(MAX_SAVED_TEXT_CHARACTERS), it.company, it.role, proposedCompany, proposedRole) }
            .distinctUntilChanged()
            .onEach { (text, company, role, proposedCompany, proposedRole) ->
                savedState[TEXT_KEY] = text
                savedState[COMPANY_KEY] = company
                savedState[ROLE_KEY] = role
                savedState[PROPOSED_COMPANY_KEY] = proposedCompany
                savedState[PROPOSED_ROLE_KEY] = proposedRole
            }
            .launchIn(viewModelScope)
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

    private fun restoreTypedInput() {
        val text = savedState.get<String>(TEXT_KEY) ?: return
        proposedCompany = savedState.get<String>(PROPOSED_COMPANY_KEY).orEmpty()
        proposedRole = savedState.get<String>(PROPOSED_ROLE_KEY).orEmpty()
        viewModelScope.launch {
            val kept = sessionRepository.observeKeptJobDescription().first()
            if (kept != null) mutableState.update { it.copy(keptText = kept.text) }
        }
        mutableState.update {
            it.copy(
                text = text,
                company = savedState.get<String>(COMPANY_KEY).orEmpty(),
                role = savedState.get<String>(ROLE_KEY).orEmpty(),
            )
        }
    }

    fun onAction(action: PasteJobDescriptionAction) {
        when (action) {
            is PasteJobDescriptionAction.TextChanged -> onTextChanged(action.value)
            is PasteJobDescriptionAction.Pasted -> onPasted(action.value)
            is PasteJobDescriptionAction.CompanyChanged -> onCompanyChanged(action.value)
            is PasteJobDescriptionAction.RoleChanged -> onRoleChanged(action.value)
            PasteJobDescriptionAction.ClearTapped -> onClear()
            PasteJobDescriptionAction.AnalyseTapped -> onAnalyse()
            PasteJobDescriptionAction.RetryTapped -> mutableState.update { it.copy(message = null) }
            PasteJobDescriptionAction.DismissMessageTapped -> mutableState.update { it.copy(message = null) }
            PasteJobDescriptionAction.NextStepConsumed -> mutableState.update { it.copy(nextStep = null) }
        }
    }

    private fun onTextChanged(value: String) {
        mutableState.update { state -> state.copy(text = value, message = null, nextStep = null) }
        labelsEditedSinceText = false
        schedulePrefill()
    }

    private fun onCompanyChanged(value: String) {
        labelsEditedSinceText = true
        mutableState.update { it.copy(company = value) }
    }

    private fun onRoleChanged(value: String) {
        labelsEditedSinceText = true
        mutableState.update { it.copy(role = value) }
    }

    private fun schedulePrefill() {
        prefillJob?.cancel()
        val text = mutableState.value.text
        if (text.isBlank()) return
        prefillJob = viewModelScope.launch {
            delay(PREFILL_DEBOUNCE_MS)
            proposeAndApplyLabels(text)
        }
    }

    private suspend fun proposeAndApplyLabels(text: String) {
        val proposal = try {
            withContext(computeDispatcher) { proposeJobLabel(text) }
        } catch (_: AiException) {
            return
        }
        mutableState.update { it.withProposedLabels(proposal) }
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
        labelsEditedSinceText = false
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
            if (prefillJob?.isActive == true && !labelsEditedSinceText) {
                prefillJob?.cancel()
                proposeAndApplyLabels(mutableState.value.text)
            }
            if (usageAllowance.observeAnalysesLeft().first() <= 0) {
                isSubmitting = false
                return@launch
            }
            val current = mutableState.value
            val kept = KeptJobDescription(
                text = current.text.trim(),
                company = current.company.trim(),
                role = current.role.trim(),
                companyIsPrefill = current.company.isUntouchedPrefill(proposedCompany),
                roleIsPrefill = current.role.isUntouchedPrefill(proposedRole),
            )
            val previous = sessionRepository.observeKeptJobDescription().first()
            if (previous != null && previous.draftKey != kept.draftKey) discardJobDrafts(previous)
            sessionRepository.keepJobDescription(kept)
            val step = nextOnboardingStep()
            isSubmitting = false
            mutableState.update { it.copy(nextStep = step, keptText = kept.text) }
        }
    }

    private fun String.isUntouchedPrefill(proposed: String): Boolean = isNotBlank() && trim() == proposed.trim()

    private companion object {
        const val PREFILL_DEBOUNCE_MS = 300L
        const val TEXT_KEY = "pasteJd.text"
        const val COMPANY_KEY = "pasteJd.company"
        const val ROLE_KEY = "pasteJd.role"
        const val PROPOSED_COMPANY_KEY = "pasteJd.proposedCompany"
        const val PROPOSED_ROLE_KEY = "pasteJd.proposedRole"
        const val MAX_SAVED_TEXT_CHARACTERS = 100_000
    }
}
