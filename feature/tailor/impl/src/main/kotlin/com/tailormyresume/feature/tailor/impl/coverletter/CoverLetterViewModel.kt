package com.tailormyresume.feature.tailor.impl.coverletter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ContentReportRepository
import com.tailormyresume.core.data.repository.CoverLetterRepository
import com.tailormyresume.core.data.repository.ExportHistoryRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.coverletter.CoverLetterComposer
import com.tailormyresume.core.domain.coverletter.CoverLetterSource
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.ContentReport
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.ReportedItemKind
import com.tailormyresume.feature.tailor.api.navigation.CoverLetterNavKey
import com.tailormyresume.feature.tailor.impl.TailorInputs
import com.tailormyresume.feature.tailor.impl.TailorUiState
import com.tailormyresume.feature.tailor.impl.buildTailorUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
class CoverLetterViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val generateCoverLetter: CoverLetterSource,
    private val connectivityMonitor: ConnectivityMonitor,
    private val contentReportRepository: ContentReportRepository,
    private val coverLetterRepository: CoverLetterRepository,
    private val exportHistoryRepository: ExportHistoryRepository,
    private val clock: Clock,
) : ViewModel() {

    private val mutableState = MutableStateFlow(CoverLetterUiState())

    private var hasEntered = false

    private var applicationId: String = ""

    private var scenario: DebugScenario = DebugScenario.defaultValue

    val uiState: StateFlow<CoverLetterUiState> = mutableState.asStateFlow()

    fun onEnter(key: CoverLetterNavKey) {
        if (hasEntered) return
        hasEntered = true
        applicationId = key.applicationId
        scenario = key.scenario
        mutableState.value = CoverLetterUiState(
            stage = coverLetterStageFor(key.scenario),
            isOffline = key.scenario == DebugScenario.OFFLINE,
        )
        viewModelScope.launch { observeConnectivity() }
        viewModelScope.launch { observeReports() }
        viewModelScope.launch { observeExportedFile() }
        viewModelScope.launch { loadOffer() }
    }

    fun onAction(action: CoverLetterAction) {
        when (action) {
            CoverLetterAction.WriteOne -> onWriteOne()
            is CoverLetterAction.BeginEdit -> onBeginEdit(action.ordinal)
            is CoverLetterAction.EditTextChanged -> onEditTextChanged(action.value)
            CoverLetterAction.SaveEdit -> onSaveEdit()
            CoverLetterAction.CancelEdit -> onCancelEdit()
            is CoverLetterAction.ReportInaccurate -> onReportInaccurate(action.ordinal)
            CoverLetterAction.DismissMessage -> onDismissMessage()
            CoverLetterAction.Retry -> onWriteOne()
        }
    }

    private suspend fun observeConnectivity() {
        connectivityMonitor.isOnline.collect { online ->
            mutableState.update { state ->
                state.copy(isOffline = !online || scenario == DebugScenario.OFFLINE)
            }
        }
    }

    private suspend fun observeReports() {
        contentReportRepository.observeReportedIds(applicationId, ReportedItemKind.COVER_LETTER).collect { ids ->
            mutableState.update { state -> state.copy(reportedIds = ids) }
        }
    }

    private suspend fun observeExportedFile() {
        exportHistoryRepository.observeExports(applicationId).collect { records ->
            mutableState.update { state -> state.copy(exportedFileName = records.lastOrNull()?.fileName) }
        }
    }

    private suspend fun loadOffer() {
        val application = applicationRepository.observeApplication(applicationId).first() ?: return
        val profile = profileRepository.observeProfile().first()
        val (reviewed, total) = application.reviewSummary(profile)
        mutableState.update { state ->
            state.copy(
                jobTitle = application.job.title,
                jobCompany = application.job.company,
                reviewedCount = reviewed,
                totalCount = total,
                factCount = profile?.let { CoverLetterComposer.evidenceCount(it, application.analysisOrEmpty()) } ?: 0,
            )
        }
        restoreWrittenLetter(application, profile)
    }

    private suspend fun restoreWrittenLetter(application: JobApplication, profile: CandidateProfile?) {
        if (scenario != DebugScenario.DEFAULT) return
        val written = coverLetterRepository.observeLetter(applicationId).first() ?: return
        val restored = restoredCoverLetterState(profile, application.analysisOrEmpty(), written) ?: return
        mutableState.update { state ->
            if (state.stage != CoverLetterStage.OFFER) {
                state
            } else {
                restored.copy(
                    isOffline = state.isOffline,
                    reviewedCount = state.reviewedCount,
                    totalCount = state.totalCount,
                    reportedIds = state.reportedIds,
                    exportedFileName = state.exportedFileName,
                )
            }
        }
    }

    private fun onWriteOne() {
        mutableState.update { state -> state.copy(stage = CoverLetterStage.GENERATING) }
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val application = applicationRepository.observeApplication(applicationId).first()
        if (application == null) {
            mutableState.update { state -> state.copy(stage = CoverLetterStage.ERROR) }
            return
        }
        val profile = profileRepository.observeProfile().first()
        val analysis = application.analysisOrEmpty()
        if (profile == null) {
            mutableState.update { state -> state.copy(stage = CoverLetterStage.EMPTY_PROFILE) }
            return
        }
        val draft = runCatching { generateCoverLetter(candidate = profile, job = application.job, analysis = analysis) }
            .getOrNull()
        if (draft == null) {
            mutableState.update { state -> state.copy(stage = CoverLetterStage.ERROR) }
            return
        }
        val generated = coverLetterStateFor(CoverLetterInputs(profile = profile, analysis = analysis, draft = draft))
        if (generated.paragraphs.isNotEmpty()) {
            coverLetterRepository.save(applicationId, generated.toWrittenLetter(clock.now()))
        }
        mutableState.update { state ->
            generated.copy(
                isOffline = state.isOffline,
                reviewedCount = state.reviewedCount,
                totalCount = state.totalCount,
                reportedIds = state.reportedIds,
                exportedFileName = state.exportedFileName,
            )
        }
    }

    private fun onBeginEdit(ordinal: Int) {
        mutableState.update { state ->
            val paragraph = state.paragraphs.firstOrNull { item -> item.ordinal == ordinal }
            if (paragraph == null) {
                state
            } else {
                state.copy(editingOrdinal = ordinal, editingText = paragraph.text, message = null)
            }
        }
    }

    private fun onEditTextChanged(value: String) {
        mutableState.update { state -> state.copy(editingText = value) }
    }

    private fun onSaveEdit() {
        val current = mutableState.value
        val ordinal = current.editingOrdinal ?: return
        val replacement = current.editingText.trim()
        if (replacement.isEmpty()) return
        val saved = current.copy(
            paragraphs = current.paragraphs.map { paragraph ->
                if (paragraph.ordinal == ordinal) paragraph.withEditedText(replacement) else paragraph
            },
            editingOrdinal = null,
            editingText = "",
            message = CoverLetterMessage.SAVED,
        )
        mutableState.value = saved
        viewModelScope.launch { coverLetterRepository.save(applicationId, saved.toWrittenLetter(clock.now())) }
    }

    private fun onCancelEdit() {
        mutableState.update { state -> state.copy(editingOrdinal = null, editingText = "") }
    }

    private fun onReportInaccurate(ordinal: Int) {
        val paragraph = mutableState.value.paragraphs.firstOrNull { it.ordinal == ordinal } ?: return
        mutableState.update { state -> state.copy(message = CoverLetterMessage.REPORTED) }
        viewModelScope.launch {
            contentReportRepository.report(
                ContentReport(
                    applicationId = applicationId,
                    itemKind = ReportedItemKind.COVER_LETTER,
                    itemId = ordinal.toString(),
                    itemText = paragraph.text,
                    reportedAt = clock.now(),
                    generationId = mutableState.value.generationId,
                ),
            )
        }
    }

    private fun onDismissMessage() {
        mutableState.update { state -> state.copy(message = null) }
    }

    private fun JobApplication.reviewSummary(profile: CandidateProfile?): Pair<Int, Int> {
        val summary = buildTailorUiState(
            TailorInputs(
                application = this,
                profile = profile,
                isOffline = false,
                regenerationsUsed = 0,
                editedBulletIds = emptySet(),
            ),
        ) as? TailorUiState.Success
        return (summary?.reviewedCount ?: 0) to (summary?.totalCount ?: 0)
    }

    private fun JobApplication.analysisOrEmpty(): JobAnalysisResult = JobAnalysisResult(
        job = job,
        gap = gapAnalysis ?: GapAnalysis(
            matches = emptyList(),
            keywordCoverage = KeywordCoverage(covered = 0, total = 0),
        ),
    )
}
