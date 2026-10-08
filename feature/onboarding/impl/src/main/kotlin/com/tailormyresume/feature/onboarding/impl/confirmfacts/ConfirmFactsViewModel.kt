package com.tailormyresume.feature.onboarding.impl.confirmfacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.ImportRemovalNotice
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.feature.onboarding.api.navigation.ConfirmFactsNavKey
import com.tailormyresume.feature.onboarding.impl.common.observeOffline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

sealed interface ConfirmFactsAction {
    data class Confirm(val factId: String) : ConfirmFactsAction
    data class Edit(val factId: String?, val category: EntryCategory) : ConfirmFactsAction
    data class AddOne(val section: ConfirmFactsSection) : ConfirmFactsAction
    data class Skip(val section: ConfirmFactsSection) : ConfirmFactsAction
    data object Continue : ConfirmFactsAction
    data object NextStepConsumed : ConfirmFactsAction
    data object EditConsumed : ConfirmFactsAction
    data object Retry : ConfirmFactsAction
}

@HiltViewModel
class ConfirmFactsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val nextOnboardingStep: NextOnboardingStepUseCase,
    private val connectivityMonitor: ConnectivityMonitor,
    private val importRemovalNotice: ImportRemovalNotice,
) : ViewModel() {

    private val profileMutex = Mutex()
    private val mutableUiState = MutableStateFlow(ConfirmFactsUiState())
    private var scenario = DebugScenario.defaultValue
    private var hasEntered = false
    private var loadJob: Job? = null

    val uiState: StateFlow<ConfirmFactsUiState> = mutableUiState.asStateFlow()

    fun onEnter(key: ConfirmFactsNavKey) {
        if (hasEntered) return
        hasEntered = true
        scenario = key.scenario
        mutableUiState.value = ConfirmFactsScenarioMapper.seed(key.scenario)
        val forcedOffline = key.scenario == DebugScenario.OFFLINE
        viewModelScope.launch {
            connectivityMonitor.observeOffline(forcedOffline).collect { offline ->
                mutableUiState.update { it.copy(isOffline = offline) }
            }
        }
        viewModelScope.launch {
            importRemovalNotice.showsBanner.collect { shows ->
                mutableUiState.update { it.copy(showsRemovedBanner = shows) }
            }
        }
        if (key.scenario != DebugScenario.LOADING) {
            startLoading()
        }
    }

    fun onAction(action: ConfirmFactsAction) {
        when (action) {
            is ConfirmFactsAction.Confirm -> confirm(action.factId)
            is ConfirmFactsAction.Edit -> {
                mutableUiState.update {
                    it.copy(pendingEdit = PendingEdit(factId = action.factId, category = action.category))
                }
            }

            is ConfirmFactsAction.AddOne -> {
                mutableUiState.update {
                    it.copy(pendingEdit = PendingEdit(factId = null, category = action.section.categoryOf()))
                }
            }

            is ConfirmFactsAction.Skip -> {
                mutableUiState.update { it.copy(skippedSections = it.skippedSections + action.section) }
            }

            ConfirmFactsAction.EditConsumed -> {
                mutableUiState.update { it.copy(pendingEdit = null) }
            }

            ConfirmFactsAction.Continue -> {
                if (!mutableUiState.value.canContinue) return
                viewModelScope.launch {
                    val step = nextOnboardingStep()
                    mutableUiState.update { it.copy(nextStep = step) }
                }
            }

            ConfirmFactsAction.NextStepConsumed -> mutableUiState.update { it.copy(nextStep = null) }
            ConfirmFactsAction.Retry -> {
                mutableUiState.update { it.copy(hasSaveFailed = false, isLoading = true) }
                startLoading()
            }
        }
    }

    private suspend fun load() {
        if (scenario == DebugScenario.DEFAULT) {
            profileRepository.observeProfile().collect { profile ->
                mutableUiState.update { ConfirmFactsScenarioMapper.withProfile(it, profile, scenario) }
            }
        } else {
            val profile = profileRepository.observeProfile().first()
            mutableUiState.update { ConfirmFactsScenarioMapper.withProfile(it, profile, scenario) }
        }
    }

    private fun startLoading() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { load() }
    }

    private fun confirm(factId: String) {
        if (mutableUiState.value.isSaving) return
        mutableUiState.update { it.copy(isSaving = true, hasSaveFailed = false) }
        viewModelScope.launch {
            val failed = runCatching {
                profileMutex.withLock {
                    val profile = profileRepository.observeProfile().first() ?: return@withLock
                    profileRepository.saveProfile(profile.confirming(factId))
                }
            }.isFailure
            mutableUiState.update { current ->
                if (failed) {
                    current.copy(isSaving = false, hasSaveFailed = true)
                } else {
                    current.copy(
                        isSaving = false,
                        hasSaveFailed = false,
                        sections = current.sections.withConfirmation(factId),
                    )
                }
            }
        }
    }

    private fun List<ConfirmFactsSectionUi>.withConfirmation(factId: String): List<ConfirmFactsSectionUi> =
        map { section ->
            section.copy(
                facts = section.facts.map { fact ->
                    if (fact.id == factId) fact.copy(isConfirmed = true) else fact
                },
            )
        }

    private fun CandidateProfile.confirming(factId: String): CandidateProfile =
        copy(entries = entries.map { entry -> if (entry.id == factId) entry.copy(isConfirmed = true) else entry })
}

fun ConfirmFactsSection.categoryOf(): EntryCategory = when (this) {
    ConfirmFactsSection.Education,
    -> EntryCategory.EDUCATION
    ConfirmFactsSection.Experience -> EntryCategory.EXPERIENCE
    ConfirmFactsSection.Projects,
    ConfirmFactsSection.Skills,
    -> EntryCategory.PROJECT
    ConfirmFactsSection.Certifications -> EntryCategory.CERTIFICATION
    ConfirmFactsSection.Extras -> EntryCategory.ACHIEVEMENT
}
