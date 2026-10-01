package com.hirehop.feature.tailor.impl.prepquestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.prep.PrepQuestionGenerator
import com.hirehop.core.domain.prep.PrepQuestionSource
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.feature.tailor.api.navigation.PrepQuestionsNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrepQuestionsViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val generatePrepQuestions: PrepQuestionSource,
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
            filter = prepQuestionsFilterFor(key.scenario),
        )
        if (prepQuestionsIsStatic(key.scenario)) return
        viewModelScope.launch { load() }
    }

    fun onAction(action: PrepQuestionsAction) {
        when (action) {
            is PrepQuestionsAction.FilterChosen -> onFilterChosen(action.filter)
            is PrepQuestionsAction.PractiseToggled -> onPractiseToggled(action.questionId)
            is PrepQuestionsAction.ReportInaccurate -> onReportInaccurate(action.questionId)
            PrepQuestionsAction.DismissMessage -> onDismissMessage()
            PrepQuestionsAction.Retry -> onRetry()
        }
    }

    private suspend fun load() {
        val application = applicationRepository.observeApplication(applicationId).first()
        if (application == null) {
            mutableState.value = PrepQuestionsUiState(stage = PrepQuestionsStage.ERROR)
            return
        }
        val profile = profileRepository.observeProfile().first()
        val analysis = application.analysisOrEmpty()
        if (profile == null) {
            mutableState.value = PrepQuestionsUiState(
                stage = PrepQuestionsStage.EMPTY_PROFILE,
                jobTitle = analysis.job.title,
                jobCompany = analysis.job.company,
                isOffline = prepQuestionsIsOffline(scenario),
                filter = prepQuestionsFilterFor(scenario),
            )
            return
        }
        val questions = runCatching {
            generatePrepQuestions(
                analysis = analysis,
                profile = profile,
                limit = PrepQuestionGenerator.MAX_QUESTIONS,
            )
        }.getOrDefault(emptyList())
        mutableState.value = prepQuestionsStateFor(
            PrepQuestionsInputs(
                profile = profile,
                questions = questions,
                jobTitle = analysis.job.title,
                jobCompany = analysis.job.company,
                isOffline = prepQuestionsIsOffline(scenario),
            ),
        ).copy(filter = prepQuestionsFilterFor(scenario))
    }

    private fun onFilterChosen(filter: PrepQuestionFilter) {
        mutableState.update { state -> state.copy(filter = filter) }
    }

    private fun onPractiseToggled(questionId: String) {
        mutableState.update { state ->
            val card = state.cardOf(questionId) ?: return@update state
            val next = !card.isPractised
            state.copy(
                groups = state.groups.map { group ->
                    group.copy(
                        cards = group.cards.map { item ->
                            if (item.id == questionId) item.copy(isPractised = next) else item
                        },
                    )
                },
                message = if (next) PrepQuestionsMessage.PRACTISED else PrepQuestionsMessage.UNPRACTISED,
            )
        }
    }

    private fun onReportInaccurate(questionId: String) {
        mutableState.update { state ->
            if (state.cardOf(questionId) != null) {
                state.copy(message = PrepQuestionsMessage.REPORT_UNAVAILABLE)
            } else {
                state
            }
        }
    }

    private fun onDismissMessage() {
        mutableState.update { state -> state.copy(message = null) }
    }

    private fun onRetry() {
        mutableState.value = PrepQuestionsUiState(
            stage = PrepQuestionsStage.GENERATING,
            filter = prepQuestionsFilterFor(scenario),
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
