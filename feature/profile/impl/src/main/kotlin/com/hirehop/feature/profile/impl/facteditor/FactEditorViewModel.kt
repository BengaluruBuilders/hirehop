package com.hirehop.feature.profile.impl.facteditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.IdGenerator
import com.hirehop.core.domain.fact.FactDraft
import com.hirehop.core.domain.fact.FactDraftError
import com.hirehop.core.domain.fact.FactDraftErrorReason
import com.hirehop.core.domain.fact.FactDraftValidator
import com.hirehop.core.domain.fact.FactField
import com.hirehop.core.domain.fact.FactIdAllocator
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
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

@HiltViewModel
class FactEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val factIdAllocator: FactIdAllocator,
    private val idGenerator: IdGenerator,
) : ViewModel() {

    private val profileMutex = Mutex()
    private val entryId: String? = savedStateHandle.get<String>(ENTRY_ID_KEY)
    private val requestedCategory: EntryCategory = categoryOf(savedStateHandle.get<String>(ENTRY_TYPE_KEY))
    private val scenario: DebugScenario = scenarioOf(savedStateHandle.get<String>(SCENARIO_KEY))

    private val mutableUiState = MutableStateFlow(
        FactEditorScenarioMapper.seed(
            scenario = scenario,
            category = requestedCategory,
            entryId = entryId,
        ),
    )

    val uiState: StateFlow<FactEditorUiState> = mutableUiState.asStateFlow()

    init {
        if (scenario != DebugScenario.LOADING) {
            viewModelScope.launch {
                val profile = profileRepository.observeProfile().first()
                mutableUiState.update { it.withLoadedProfile(profile) }
            }
        }
    }

    fun onTitleChange(value: String) = onFieldChange(FactField.TITLE) { it.copy(title = value) }

    fun onDetailChange(value: String) = onFieldChange(FactField.DETAIL) { it.copy(detail = value) }

    fun onToolsChange(value: String) = onFieldChange(FactField.ORGANIZATION) { it.copy(organization = value) }

    fun onStartDateChange(value: String) = onFieldChange(FactField.START_DATE) { it.copy(startDate = value) }

    fun onEndDateChange(value: String) = onFieldChange(FactField.END_DATE) { it.copy(endDate = value) }

    fun save() {
        val current = mutableUiState.value
        val errors = FactDraftValidator.validate(current.draft).toFieldErrorMap()
        if (current.draft.title.isBlank() || errors.isNotEmpty()) {
            mutableUiState.update { it.copy(fieldErrors = errors, touchedFields = FactField.entries.toSet()) }
            return
        }
        mutableUiState.update {
            it.copy(isSaving = true, isSaveFailed = false, fieldErrors = emptyMap(), touchedFields = emptySet())
        }
        viewModelScope.launch {
            val source = runCatching { persist(current.draft, current.factId) }
            mutableUiState.update {
                if (source.isFailure) {
                    it.copy(isSaving = false, isSaveFailed = true)
                } else {
                    it.copy(
                        isSaving = false,
                        outcome = FactEditorOutcome.Saved,
                        wasQueued = it.isOffline,
                        provenance = source.getOrThrow(),
                    )
                }
            }
        }
    }

    fun requestDelete() {
        mutableUiState.update { it.copy(isDeleteDialogVisible = true) }
    }

    fun dismissDelete() {
        mutableUiState.update { it.copy(isDeleteDialogVisible = false) }
    }

    fun confirmDelete() {
        val targetId = mutableUiState.value.factId
        mutableUiState.update { it.copy(isDeleteDialogVisible = false, isSaving = true) }
        viewModelScope.launch {
            val failed = runCatching {
                profileMutex.withLock {
                    val profile = profileRepository.observeProfile().first() ?: return@withLock
                    profileRepository.saveProfile(profile.withoutEntry(targetId))
                }
            }.isFailure
            mutableUiState.update {
                if (failed) {
                    it.copy(isSaving = false, isSaveFailed = true)
                } else {
                    it.copy(isSaving = false, outcome = FactEditorOutcome.Deleted)
                }
            }
        }
    }

    fun cancel() {
        mutableUiState.update { it.copy(outcome = FactEditorOutcome.Cancelled) }
    }

    private fun onFieldChange(
        field: FactField,
        transform: (FactDraft) -> FactDraft,
    ) {
        mutableUiState.update { current ->
            val draft = transform(current.draft)
            current.copy(
                draft = draft,
                fieldErrors = FactDraftValidator.validate(draft).toFieldErrorMap(),
                touchedFields = current.touchedFields + field,
                isSaveFailed = false,
            )
        }
    }

    private suspend fun persist(draft: FactDraft, id: String): FactSource = profileMutex.withLock {
        val profile = profileRepository.observeProfile().first()
        val source = sourceFor(profile, id)
        if (profile != null) {
            profileRepository.saveProfile(profile.withFact(draft, id))
        }
        source
    }

    private fun FactEditorUiState.withLoadedProfile(profile: CandidateProfile?): FactEditorUiState {
        val loaded = if (scenario == DebugScenario.EMPTY) null else profile?.findEntry(factId)
        if (loaded == null) {
            val existing = profile?.entries.orEmpty()
            return FactEditorScenarioMapper.withoutEntry(
                state = copy(isLoading = false),
                nextId = factIdAllocator.nextId(requestedCategory, existing),
            )
        }
        return FactEditorScenarioMapper.withEntry(state = this, entry = loaded, scenario = scenario)
    }

    private fun CandidateProfile?.findEntry(id: String): ProfileEntry? =
        this?.entries?.firstOrNull { it.id == id }

    private suspend fun sourceFor(
        profile: CandidateProfile?,
        id: String,
    ): FactSource =
        if (profile.findEntry(id) == null) FactSource.USER_STATED else FactSource.USER_EDITED

    private fun CandidateProfile.withoutEntry(id: String): CandidateProfile =
        copy(entries = entries.filterNot { it.id == id })

    private fun CandidateProfile.withFact(draft: FactDraft, id: String): CandidateProfile {
        val existing = findEntry(id)
        val entry = draft.toEntry(
            id = id,
            source = if (existing == null) FactSource.USER_STATED else FactSource.USER_EDITED,
            existingBulletId = existing?.bullets?.firstOrNull()?.id,
            newBulletId = idGenerator.newId(),
            isConfirmed = true,
        )
        val updated = if (existing == null) entries + entry else entries.map { if (it.id == id) entry else it }
        return copy(entries = updated)
    }

    private companion object {
        const val ENTRY_ID_KEY = "entryId"
        const val ENTRY_TYPE_KEY = "entryType"
        const val SCENARIO_KEY = "scenario"
    }
}

private fun scenarioOf(raw: String?): DebugScenario =
    DebugScenario.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
        ?: DebugScenario.defaultValue

private fun List<FactDraftError>.toFieldErrorMap(): Map<FactField, FactDraftErrorReason> =
    associate { it.field to it.reason }
