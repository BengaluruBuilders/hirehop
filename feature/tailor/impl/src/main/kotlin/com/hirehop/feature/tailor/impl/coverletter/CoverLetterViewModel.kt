package com.hirehop.feature.tailor.impl.coverletter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.coverletter.CoverLetterSource
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.feature.tailor.api.navigation.CoverLetterNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoverLetterViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val generateCoverLetter: CoverLetterSource,
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
        mutableState.value = CoverLetterUiState(stage = coverLetterStageFor(key.scenario))
        if (coverLetterIsStatic(key.scenario)) return
        viewModelScope.launch { load() }
    }

    fun onAction(action: CoverLetterAction) {
        when (action) {
            is CoverLetterAction.BeginEdit -> onBeginEdit(action.ordinal)
            is CoverLetterAction.EditTextChanged -> onEditTextChanged(action.value)
            CoverLetterAction.SaveEdit -> onSaveEdit()
            CoverLetterAction.CancelEdit -> onCancelEdit()
            is CoverLetterAction.CopyLetter -> onCopyLetter(action.letterText)
            is CoverLetterAction.ReportInaccurate -> onReportInaccurate(action.ordinal)
            CoverLetterAction.DismissMessage -> onDismissMessage()
            CoverLetterAction.Retry -> onRetry()
        }
    }

    private suspend fun load() {
        val application = applicationRepository.observeApplication(applicationId).first()
        if (application == null) {
            mutableState.value = CoverLetterUiState(stage = CoverLetterStage.ERROR)
            return
        }
        val profile = profileRepository.observeProfile().first()
        val analysis = application.analysisOrEmpty()
        if (profile == null) {
            mutableState.value = CoverLetterUiState(
                stage = CoverLetterStage.EMPTY_PROFILE,
                jobTitle = analysis.job.title,
                jobCompany = analysis.job.company,
                isOffline = coverLetterIsOffline(scenario),
            )
            return
        }
        val draft = runCatching { generateCoverLetter(candidate = profile, job = application.job, analysis = analysis) }
            .getOrNull()
        if (draft == null) {
            mutableState.value = CoverLetterUiState(
                stage = CoverLetterStage.ERROR,
                jobTitle = analysis.job.title,
                jobCompany = analysis.job.company,
                isOffline = coverLetterIsOffline(scenario),
            )
            return
        }
        mutableState.value = coverLetterStateFor(
            CoverLetterInputs(
                profile = profile,
                analysis = analysis,
                draft = draft,
                isOffline = coverLetterIsOffline(scenario),
            ),
        )
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
        mutableState.update { state ->
            val ordinal = state.editingOrdinal ?: return@update state
            val replacement = state.editingText.trim()
            if (replacement.isEmpty()) {
                return@update state
            }
            state.copy(
                paragraphs = state.paragraphs.map { paragraph ->
                    if (paragraph.ordinal == ordinal) {
                        paragraph.copy(
                            text = replacement,
                            isUserEdited = true,
                            sentences = replacement.sentences().map { sentence ->
                                CoverLetterSentence(text = sentence, factId = null)
                            },
                        )
                    } else {
                        paragraph
                    }
                },
                editingOrdinal = null,
                editingText = "",
                message = CoverLetterMessage.SAVED,
            )
        }
    }

    private fun onCancelEdit() {
        mutableState.update { state -> state.copy(editingOrdinal = null, editingText = "") }
    }

    private fun onCopyLetter(letterText: String) {
        mutableState.update { state -> state.copy(message = CoverLetterMessage.COPIED, copiedText = letterText) }
    }

    private fun onReportInaccurate(ordinal: Int) {
        mutableState.update { state ->
            val known = state.paragraphs.any { paragraph -> paragraph.ordinal == ordinal }
            if (known) {
                state.copy(message = CoverLetterMessage.REPORT_UNAVAILABLE)
            } else {
                state
            }
        }
    }

    private fun onDismissMessage() {
        mutableState.update { state -> state.copy(message = null) }
    }

    private fun onRetry() {
        mutableState.value = CoverLetterUiState(
            stage = CoverLetterStage.GENERATING,
            isOffline = coverLetterIsOffline(scenario),
        )
        viewModelScope.launch { load() }
    }

    private fun JobApplication.analysisOrEmpty(): JobAnalysisResult = JobAnalysisResult(
        job = job,
        gap = gapAnalysis ?: GapAnalysis(
            matches = emptyList(),
            keywordCoverage = KeywordCoverage(covered = 0, total = 0),
        ),
    )
}
