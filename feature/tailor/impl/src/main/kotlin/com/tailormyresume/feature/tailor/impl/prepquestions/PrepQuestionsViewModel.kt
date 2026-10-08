package com.tailormyresume.feature.tailor.impl.prepquestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ContentReportRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.prep.PrepQuestionGenerator
import com.tailormyresume.core.domain.prep.PrepQuestionSource
import com.tailormyresume.core.model.ContentReport
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.ReportedItemKind
import com.tailormyresume.feature.tailor.api.navigation.PrepQuestionsNavKey
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
class PrepQuestionsViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val generatePrepQuestions: PrepQuestionSource,
    private val connectivityMonitor: ConnectivityMonitor,
    private val contentReportRepository: ContentReportRepository,
    private val clock: Clock,
) : ViewModel() {

    private val mutableState = MutableStateFlow(PrepQuestionsUiState())

    private var hasEntered = false

    private var applicationId: String = ""

    private var scenario: DebugScenario = DebugScenario.defaultValue

    val uiState: StateFlow<PrepQuestionsUiState> = mutableState.asStateFlow()

    fun onEnter(key: PrepQuestionsNavKey) {
        if (hasEntered) return
        hasEntered = true
        applicationId = key.applicationId
        scenario = key.scenario
        mutableState.value = PrepQuestionsUiState(
            stage = prepQuestionsStageFor(key.scenario),
            isOffline = key.scenario == DebugScenario.OFFLINE,
        )
        viewModelScope.launch { observeConnectivity() }
        viewModelScope.launch { observeReports() }
        if (prepQuestionsIsStatic(key.scenario)) {
            viewModelScope.launch { loadHeader() }
            return
        }
        viewModelScope.launch { load() }
    }

    fun onAction(action: PrepQuestionsAction) {
        when (action) {
            is PrepQuestionsAction.ReportInaccurate -> onReportInaccurate(action.questionId)
            PrepQuestionsAction.DismissMessage -> onDismissMessage()
            PrepQuestionsAction.Retry -> onRetry()
        }
    }

    private suspend fun observeConnectivity() {
        connectivityMonitor.isOnline.collect { online ->
            mutableState.update { state -> state.copy(isOffline = !online || scenario == DebugScenario.OFFLINE) }
        }
    }

    private suspend fun observeReports() {
        contentReportRepository.observeReportedIds(applicationId, ReportedItemKind.PREP_QUESTION).collect { ids ->
            mutableState.update { state -> state.copy(reportedIds = ids) }
        }
    }

    private suspend fun loadHeader() {
        val application = applicationRepository.observeApplication(applicationId).first() ?: return
        mutableState.update { state ->
            state.copy(jobTitle = application.job.title, jobCompany = application.job.company)
        }
    }

    private suspend fun load() {
        val application = applicationRepository.observeApplication(applicationId).first()
        if (application == null) {
            mutableState.update { state -> state.copy(stage = PrepQuestionsStage.ERROR) }
            return
        }
        val profile = profileRepository.observeProfile().first()
        val analysis = application.analysisOrEmpty()
        if (profile == null) {
            mutableState.update { state ->
                state.copy(
                    stage = PrepQuestionsStage.EMPTY_PROFILE,
                    jobTitle = analysis.job.title,
                    jobCompany = analysis.job.company,
                )
            }
            return
        }
        val questions = runCatching {
            generatePrepQuestions(
                analysis = analysis,
                profile = profile,
                limit = PrepQuestionGenerator.MAX_QUESTIONS,
            )
        }.getOrNull()
        if (questions == null) {
            mutableState.update { state -> state.copy(stage = PrepQuestionsStage.ERROR) }
            return
        }
        val generated = prepQuestionsStateFor(
            PrepQuestionsInputs(
                profile = profile,
                questions = questions,
                jobTitle = analysis.job.title,
                jobCompany = analysis.job.company,
            ),
        )
        mutableState.update { state -> generated.copy(isOffline = state.isOffline, reportedIds = state.reportedIds) }
    }

    private fun onReportInaccurate(questionId: String) {
        val card = mutableState.value.cardOf(questionId) ?: return
        mutableState.update { state -> state.copy(message = PrepQuestionsMessage.REPORTED) }
        viewModelScope.launch {
            contentReportRepository.report(
                ContentReport(
                    applicationId = applicationId,
                    itemKind = ReportedItemKind.PREP_QUESTION,
                    itemId = questionId,
                    itemText = card.prompt,
                    reportedAt = clock.now(),
                    generationId = card.generationId,
                ),
            )
        }
    }

    private fun onDismissMessage() {
        mutableState.update { state -> state.copy(message = null) }
    }

    private fun onRetry() {
        mutableState.update { state -> state.copy(stage = PrepQuestionsStage.GENERATING) }
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
