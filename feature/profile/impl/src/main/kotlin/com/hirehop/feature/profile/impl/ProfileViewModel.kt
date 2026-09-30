package com.hirehop.feature.profile.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.common.network.Dispatcher
import com.hirehop.core.common.network.HhDispatchers
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.IdGenerator
import com.hirehop.core.domain.ResumeTextParser
import com.hirehop.core.model.CandidateProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val resumeTextParser: ResumeTextParser,
    idGenerator: IdGenerator,
    @Dispatcher(HhDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val entryMapper = EntryDraftMapper(idGenerator)
    private val profileMutex = Mutex()
    private var parseJob: Job? = null
    private val mutableImportState = MutableStateFlow(
        ResumeImportState(rawText = savedStateHandle.get<String>(RESUME_TEXT_KEY).orEmpty()),
    )

    val uiState: StateFlow<ProfileUiState> = profileRepository.observeProfile()
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState.Loading,
        )

    val importState: StateFlow<ResumeImportState> = mutableImportState.asStateFlow()

    fun confirmEntry(entryId: String) = updateProfile { it.confirmEntry(entryId) }

    fun confirmAll() = updateProfile { it.confirmAll() }

    fun deleteEntry(entryId: String) = updateProfile { it.deleteEntry(entryId) }

    fun saveEntry(entryId: String?, draft: EntryDraft) = updateProfile { profile ->
        val existing = entryId?.let { id -> profile.entries.firstOrNull { it.id == id } }
        profile.upsertEntry(entryMapper.toEntry(existing, draft))
    }

    fun updateContact(contact: ContactDraft) = updateProfile { it.withContact(contact) }

    fun addSkill(skill: String) = updateProfile { it.withSkill(skill) }

    fun removeSkill(skill: String) = updateProfile { it.withoutSkill(skill) }

    fun loadDemoProfile() = replaceProfile { DemoProfileProvider.profile() }

    fun startManualProfile() = replaceProfile { blankProfile() }

    fun clearProfile() {
        resetImport()
        viewModelScope.launch {
            profileMutex.withLock { profileRepository.clearProfile() }
        }
    }

    fun onResumeTextChange(text: String) {
        parseJob?.cancel()
        savedStateHandle[RESUME_TEXT_KEY] = text
        mutableImportState.value = ResumeImportState(rawText = text)
    }

    fun parseResume() {
        val current = mutableImportState.value
        if (current.rawText.isBlank() || current.isParsing) return
        mutableImportState.value = current.copy(isParsing = true)
        parseJob = viewModelScope.launch {
            val parsed = withContext(defaultDispatcher) { resumeTextParser.parse(current.rawText) }
            mutableImportState.update {
                if (it.rawText == current.rawText) it.copy(preview = parsed, isParsing = false) else it
            }
        }
    }

    fun savePreview() {
        val preview = mutableImportState.value.preview ?: return
        replaceProfile { preview.asUnconfirmedImport() }
        resetImport()
    }

    fun resetImport() {
        parseJob?.cancel()
        savedStateHandle[RESUME_TEXT_KEY] = ""
        mutableImportState.value = ResumeImportState()
    }

    private fun replaceProfile(create: () -> CandidateProfile) {
        viewModelScope.launch {
            profileMutex.withLock { profileRepository.saveProfile(create()) }
        }
    }

    private fun updateProfile(transform: (CandidateProfile) -> CandidateProfile) {
        viewModelScope.launch {
            profileMutex.withLock {
                val current = profileRepository.observeProfile().first() ?: return@withLock
                profileRepository.saveProfile(transform(current))
            }
        }
    }

    private fun CandidateProfile?.toUiState(): ProfileUiState =
        if (this == null) {
            ProfileUiState.Empty
        } else {
            ProfileUiState.Success(profile = this, unconfirmedCount = unconfirmedCount())
        }

    private fun blankProfile() = CandidateProfile(
        fullName = "",
        email = "",
        phone = "",
        headline = "",
        skills = emptyList(),
        entries = emptyList(),
    )

    private companion object {
        const val RESUME_TEXT_KEY = "resumeText"
    }
}
