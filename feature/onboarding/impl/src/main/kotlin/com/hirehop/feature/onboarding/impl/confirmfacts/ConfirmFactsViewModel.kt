package com.hirehop.feature.onboarding.impl.confirmfacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.feature.onboarding.api.navigation.ConfirmFactsNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
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
    data class Delete(val factId: String) : ConfirmFactsAction
    data class AddOne(val section: ConfirmFactsSection) : ConfirmFactsAction
    data class Skip(val section: ConfirmFactsSection) : ConfirmFactsAction
    data object Continue : ConfirmFactsAction
    data object EditConsumed : ConfirmFactsAction
    data object DismissRemovedNotice : ConfirmFactsAction
    data object Retry : ConfirmFactsAction
}

@HiltViewModel
class ConfirmFactsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val profileMutex = Mutex()
    private val mutableUiState = MutableStateFlow(ConfirmFactsUiState())
    private var scenario = DebugScenario.defaultValue
    private var hasEntered = false

    val uiState: StateFlow<ConfirmFactsUiState> = mutableUiState.asStateFlow()

    fun onEnter(key: ConfirmFactsNavKey) {
        if (hasEntered) return
        hasEntered = true
        scenario = key.scenario
        mutableUiState.value = ConfirmFactsScenarioMapper.seed(key.scenario)
        if (key.scenario != DebugScenario.LOADING) {
            viewModelScope.launch { load() }
        }
    }

    fun onAction(action: ConfirmFactsAction) {
        when (action) {
            is ConfirmFactsAction.Confirm -> confirm(action.factId)
            is ConfirmFactsAction.Delete -> delete(action.factId)
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

            ConfirmFactsAction.DismissRemovedNotice -> {
                mutableUiState.update { it.copy(showRemovedNotice = false) }
            }

            ConfirmFactsAction.Continue -> Unit
            ConfirmFactsAction.Retry -> {
                mutableUiState.update { it.copy(hasSaveFailed = false, isLoading = true) }
                viewModelScope.launch { load() }
            }
        }
    }

    private suspend fun load() {
        val profile = profileRepository.observeProfile().first()
        mutableUiState.update { ConfirmFactsScenarioMapper.withProfile(it, profile, scenario) }
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

    private fun delete(factId: String) {
        if (mutableUiState.value.isSaving) return
        mutableUiState.update { it.copy(isSaving = true, hasSaveFailed = false) }
        viewModelScope.launch {
            val failed = runCatching {
                profileMutex.withLock {
                    val profile = profileRepository.observeProfile().first() ?: return@withLock
                    profileRepository.saveProfile(profile.without(factId))
                }
            }.isFailure
            mutableUiState.update { current ->
                if (failed) {
                    current.copy(isSaving = false, hasSaveFailed = true)
                } else {
                    current.copy(
                        isSaving = false,
                        hasSaveFailed = false,
                        sections = current.sections.without(factId),
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

    private fun List<ConfirmFactsSectionUi>.without(factId: String): List<ConfirmFactsSectionUi> =
        map { section -> section.copy(facts = section.facts.filterNot { it.id == factId }) }

    private fun CandidateProfile.confirming(factId: String): CandidateProfile =
        copy(entries = entries.map { entry -> if (entry.id == factId) entry.copy(isConfirmed = true) else entry })

    private fun CandidateProfile.without(factId: String): CandidateProfile =
        copy(entries = entries.filterNot { it.id == factId })
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
