package com.hirehop.feature.onboarding.impl.importresume

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.ResumeTextParser
import com.hirehop.core.domain.fact.FactIdAllocator
import com.hirehop.core.domain.fact.FactLineRenderer
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import com.hirehop.feature.onboarding.api.navigation.ImportResumeNavKey
import com.hirehop.feature.onboarding.impl.common.observeOffline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportResumeViewModel @Inject constructor(
    private val resumeTextSource: ResumeTextSource,
    private val resumeTextParser: ResumeTextParser,
    private val factIdAllocator: FactIdAllocator,
    private val profileRepository: ProfileRepository,
    private val connectivityMonitor: ConnectivityMonitor,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(ImportResumeUiState())

    private var hasEntered = false

    private var queuedFile: ResumeFile? = null

    internal var retainedText: String? = null
        private set

    val uiState: StateFlow<ImportResumeUiState> = mutableUiState.asStateFlow()

    fun onEnter(key: ImportResumeNavKey) {
        if (hasEntered) return
        hasEntered = true
        mutableUiState.value = ImportResumeScenarioMapper.seed(key.scenario)
        val forcedOffline = key.scenario == DebugScenario.OFFLINE
        viewModelScope.launch {
            connectivityMonitor.observeOffline(forcedOffline).collect { offline ->
                mutableUiState.update { it.copy(isOffline = offline) }
                val waiting = queuedFile
                if (!offline && waiting != null) {
                    queuedFile = null
                    startReading(waiting)
                }
            }
        }
    }

    fun onPickRequested() {
        if (mutableUiState.value.isBusy) return
        mutableUiState.update { it.copy(stage = ImportStage.Picking) }
    }

    fun onPickerDismissed() {
        mutableUiState.update { it.copy(stage = ImportStage.Idle) }
    }

    fun onFileChosen(file: ResumeFile) {
        if (mutableUiState.value.isOffline) {
            queuedFile = file
            mutableUiState.update {
                it.copy(
                    stage = ImportStage.Idle,
                    fileName = file.displayName,
                    byteSize = file.byteSize,
                    isQueued = true,
                )
            }
            return
        }
        startReading(file)
    }

    private fun startReading(file: ResumeFile) {
        mutableUiState.update {
            it.copy(
                stage = ImportStage.Parsing,
                fileName = file.displayName,
                byteSize = file.byteSize,
                readStepIndex = 0,
                facts = emptyList(),
                isQueued = false,
            )
        }
        viewModelScope.launch { readAndParse(file) }
    }

    fun onReadOutcome(read: ResumeRead) {
        mutableUiState.update { current ->
            current.copy(
                stage = read.stage(),
                fileName = read.displayNameOr(current.fileName),
                readStepIndex = READ_STEP_COUNT,
            )
        }
    }

    fun onRetry() {
        mutableUiState.update { it.copy(stage = ImportStage.Idle) }
    }

    fun onChooseAnotherFile() {
        retainedText = null
        queuedFile = null
        mutableUiState.update {
            it.copy(stage = ImportStage.Idle, readStepIndex = 0, facts = emptyList(), isQueued = false)
        }
    }

    fun onStartGuidedForm() {
        retainedText = null
        queuedFile = null
        mutableUiState.update { it.copy(stage = ImportStage.Idle, readStepIndex = 0, facts = emptyList()) }
    }

    private suspend fun readAndParse(file: ResumeFile) {
        val read = runCatching { resumeTextSource.read(file) }.getOrNull()
        if (read == null) {
            onReadOutcome(ResumeRead.Unreadable(file.displayName))
            return
        }
        if (read !is ResumeRead.Text) {
            onReadOutcome(read)
            return
        }
        mutableUiState.update { it.copy(readStepIndex = 1) }
        retainedText = read.text
        val parsed = runCatching { resumeTextParser.parse(retainedText.orEmpty()) }.getOrNull()
        retainedText = null
        if (parsed == null) {
            onReadOutcome(ResumeRead.Unreadable(file.displayName))
            return
        }
        onParsed(profile = parsed)
    }

    private suspend fun onParsed(profile: CandidateProfile) {
        val prepared = profile.copy(entries = profile.entries.withFreshFactIds())
        val stored = runCatching {
            val existing = profileRepository.observeProfile().first()
            profileRepository.saveProfile(prepared.keepingUserFactsFrom(existing))
        }.isSuccess
        if (!stored) {
            onReadOutcome(ResumeRead.Unreadable(mutableUiState.value.fileName))
            return
        }
        mutableUiState.update {
            it.copy(
                stage = if (prepared.hasFact()) ImportStage.Success else ImportStage.NoFactsFound,
                readStepIndex = READ_STEP_COUNT,
                facts = prepared.entries.map(ProfileEntry::toImportedFact),
                skillCount = prepared.skills.size,
                isQueued = false,
            )
        }
    }

    private fun List<ProfileEntry>.withFreshFactIds(): List<ProfileEntry> {
        val allocated = mutableListOf<ProfileEntry>()
        return map { entry ->
            val reidentified = entry.copy(
                id = factIdAllocator.nextId(entry.category, allocated, entry.title),
                source = FactSource.IMPORTED,
                isConfirmed = false,
            )
            allocated += reidentified
            reidentified
        }
    }

    private fun CandidateProfile.hasFact(): Boolean =
        entries.isNotEmpty() || skills.isNotEmpty() || fullName.isNotBlank()

    private fun CandidateProfile.keepingUserFactsFrom(existing: CandidateProfile?): CandidateProfile {
        val kept = existing?.entries.orEmpty().filter { entry -> entry.source != FactSource.IMPORTED }
        return copy(entries = kept + entries)
    }
}

private fun ProfileEntry.toImportedFact(): ImportedFactUi = ImportedFactUi(
    id = id,
    category = category,
    line = FactLineRenderer.render(this),
)

private fun ResumeRead.stage(): ImportStage = when (this) {
    is ResumeRead.Text -> ImportStage.Parsing
    is ResumeRead.Empty -> ImportStage.Empty
    is ResumeRead.NoTextLayer -> ImportStage.ScannedNoText
    is ResumeRead.Unsupported -> ImportStage.Unsupported
    is ResumeRead.TooLarge -> ImportStage.TooLarge
    is ResumeRead.Unreadable -> ImportStage.Failed
}

private fun ResumeRead.displayNameOr(fallback: String): String = when (this) {
    is ResumeRead.Text -> fallback
    is ResumeRead.Empty -> fallback
    is ResumeRead.NoTextLayer -> displayName
    is ResumeRead.Unsupported -> displayName
    is ResumeRead.TooLarge -> fallback
    is ResumeRead.Unreadable -> displayName
}
