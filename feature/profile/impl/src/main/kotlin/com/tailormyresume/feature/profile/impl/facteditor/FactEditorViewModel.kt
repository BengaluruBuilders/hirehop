package com.tailormyresume.feature.profile.impl.facteditor

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.IdGenerator
import com.tailormyresume.core.domain.fact.FactDateFormat
import com.tailormyresume.core.domain.fact.FactDisplayIds
import com.tailormyresume.core.domain.fact.FactDraft
import com.tailormyresume.core.domain.fact.FactDraftError
import com.tailormyresume.core.domain.fact.FactDraftErrorReason
import com.tailormyresume.core.domain.fact.FactDraftValidator
import com.tailormyresume.core.domain.fact.FactField
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.feature.profile.api.navigation.FactEditorNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@SuppressLint("VisibleForTests")
@HiltViewModel(assistedFactory = FactEditorViewModel.Factory::class)
class FactEditorViewModel @AssistedInject constructor(
    private val profileRepository: ProfileRepository,
    private val factIdAllocator: FactIdAllocator,
    private val idGenerator: IdGenerator,
    private val connectivityMonitor: ConnectivityMonitor,
    @Assisted key: FactEditorNavKey,
    private val savedState: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    private val profileMutex = Mutex()
    private val entryId: String? = key.entryId
    private val requestedCategory: EntryCategory = categoryOf(key.entryType)
    private val scenario: DebugScenario = key.scenario
    private var existingEntries: List<ProfileEntry> = emptyList()

    private val mutableUiState = MutableStateFlow(
        FactEditorScenarioMapper.seed(
            scenario = scenario,
            category = requestedCategory,
            entryId = entryId,
        ),
    )

    val uiState: StateFlow<FactEditorUiState> = mutableUiState.asStateFlow()

    init {
        connectivityMonitor.isOnline
            .onEach { online -> mutableUiState.update { it.copy(isOffline = !online || scenario == DebugScenario.OFFLINE) } }
            .launchIn(viewModelScope)
        if (scenario != DebugScenario.LOADING) {
            viewModelScope.launch {
                val profile = profileRepository.observeProfile().first()
                mutableUiState.update { it.withLoadedProfile(profile).withSavedDraft() }
            }
        }
    }

    fun onTitleChange(value: String) {
        onFieldChange(FactField.TITLE) { it.copy(title = value) }
        mutableUiState.update { state ->
            if (state.mode == FactEditorMode.New && !state.isLoading) state.withNewId(value) else state
        }
    }

    fun onDetailChange(value: String) = onFieldChange(FactField.DETAIL) { it.copy(detail = value) }

    fun onMoreBulletChange(index: Int, value: String) = onFieldChange(FactField.DETAIL) { draft ->
        draft.copy(moreBullets = draft.moreBullets.mapIndexed { i, bullet -> if (i == index) bullet.copy(text = value) else bullet })
    }

    fun onToolsChange(value: String) = onFieldChange(FactField.ORGANIZATION) { it.copy(organization = value) }

    fun onStartDateChange(value: String) = onFieldChange(FactField.START_DATE) { it.copy(startDate = value) }

    fun onEndDateChange(value: String) = onFieldChange(FactField.END_DATE) { it.copy(endDate = value) }

    fun save() {
        val current = mutableUiState.value
        val errors = dateFormatErrors(current.draft) + FactDraftValidator.validate(current.draft).toFieldErrorMap()
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
            saveDraft(draft)
            val dateErrors = dateFormatErrors(draft)
            current.copy(
                draft = draft,
                fieldErrors = dateErrors + FactDraftValidator.validate(draft).toFieldErrorMap(),
                touchedFields = current.touchedFields + field + dateErrors.keys,
                isSaveFailed = false,
            )
        }
    }

    private fun dateFormatErrors(draft: FactDraft): Map<FactField, FactDraftErrorReason> = buildMap {
        if (!FactDateFormat.isReadable(draft.startDate, isEnd = false)) {
            put(FactField.START_DATE, FactDraftErrorReason.INVALID_DATE)
        }
        if (!FactDateFormat.isReadable(draft.endDate)) {
            put(FactField.END_DATE, FactDraftErrorReason.INVALID_DATE)
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
            existingEntries = profile?.entries.orEmpty()
            return FactEditorScenarioMapper.withoutEntry(
                state = copy(isLoading = false),
                nextId = factIdAllocator.nextId(requestedCategory, existingEntries, draft.title),
            )
        }
        val withEntry = FactEditorScenarioMapper.withEntry(
            state = this,
            entry = loaded,
            scenario = scenario,
            displayId = FactDisplayIds.of(loaded, profile?.entries.orEmpty()),
        )
        val loadedErrors = FactDraftValidator.validate(withEntry.draft).toFieldErrorMap()
        return withEntry.copy(fieldErrors = loadedErrors, touchedFields = loadedErrors.keys)
    }

    private fun saveDraft(draft: FactDraft) {
        savedState[DRAFT_KEY] = arrayListOf(
            draft.title.take(SAVED_SHORT_LIMIT),
            draft.detail.take(SAVED_DETAIL_LIMIT),
            draft.organization.take(SAVED_SHORT_LIMIT),
            draft.startDate.take(SAVED_SHORT_LIMIT),
            draft.endDate.take(SAVED_SHORT_LIMIT),
        )
        savedState[MORE_BULLETS_KEY] = ArrayList(draft.moreBullets.map { it.text.take(SAVED_DETAIL_LIMIT) })
    }

    private fun FactEditorUiState.withSavedDraft(): FactEditorUiState {
        val (title, detail, organization, startDate, endDate) = savedState.get<ArrayList<String>>(DRAFT_KEY) ?: return this
        val moreTexts = savedState.get<ArrayList<String>>(MORE_BULLETS_KEY).orEmpty()
        val restoredMore = if (moreTexts.size == draft.moreBullets.size) {
            draft.moreBullets.zip(moreTexts) { bullet, text -> bullet.copy(text = text) }
        } else {
            draft.moreBullets
        }
        val restoredDraft = draft.copy(
            title = title,
            detail = detail,
            organization = organization,
            startDate = startDate,
            endDate = endDate,
            moreBullets = restoredMore,
        )
        val restoredErrors = dateFormatErrors(restoredDraft) + FactDraftValidator.validate(restoredDraft).toFieldErrorMap()
        val restored = copy(
            draft = restoredDraft,
            fieldErrors = restoredErrors,
            touchedFields = touchedFields + restoredErrors.keys,
        )
        return if (mode == FactEditorMode.New) restored.withNewId(title) else restored
    }

    private fun FactEditorUiState.withNewId(title: String): FactEditorUiState {
        val nextId = factIdAllocator.nextId(requestedCategory, existingEntries, title)
        return copy(factId = nextId, displayId = nextId)
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
        val unedited = draft.toEntry(
            id = id,
            source = existing?.source ?: FactSource.USER_STATED,
            existingBullet = existing?.bullets?.firstOrNull(),
            newBulletId = idGenerator.newId(),
            isConfirmed = true,
        )
        val entry = if (existing == null || unedited == existing) unedited else unedited.copy(source = FactSource.USER_EDITED)
        val updated = if (existing == null) entries + entry else entries.map { if (it.id == id) entry else it }
        return copy(entries = updated)
    }

    private companion object {
        const val DRAFT_KEY = "factEditor.draft"
        const val MORE_BULLETS_KEY = "factEditor.moreBullets"
        const val SAVED_DETAIL_LIMIT = 2 * FactDraftValidator.DETAIL_LIMIT
        const val SAVED_SHORT_LIMIT = 300
    }

    @AssistedFactory
    interface Factory {
        fun create(key: FactEditorNavKey): FactEditorViewModel
    }
}

private fun List<FactDraftError>.toFieldErrorMap(): Map<FactField, FactDraftErrorReason> =
    associate { it.field to it.reason }
