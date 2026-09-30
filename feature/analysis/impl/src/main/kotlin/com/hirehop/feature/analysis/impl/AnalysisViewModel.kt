package com.hirehop.feature.analysis.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.common.network.Dispatcher
import com.hirehop.core.common.network.HhDispatchers
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.AddUserStatedFactUseCase
import com.hirehop.core.domain.AnalyzeJobUseCase
import com.hirehop.core.domain.CreateApplicationUseCase
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.model.CandidateProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val applicationRepository: ApplicationRepository,
    private val analyzeJob: AnalyzeJobUseCase,
    private val addUserStatedFact: AddUserStatedFactUseCase,
    private val createApplication: CreateApplicationUseCase,
    private val savedStateHandle: SavedStateHandle,
    @param:Dispatcher(HhDispatchers.Default) private val computeDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val jobText = savedStateHandle.getStateFlow(JOB_TEXT_KEY, "")
    private val session = MutableStateFlow<AnalysisSession>(AnalysisSession.Idle)

    val uiState: StateFlow<AnalysisUiState> = combine(
        profileRepository.observeProfile(),
        jobText,
        session,
        ::buildUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalysisUiState.Loading,
    )

    fun onJobTextChange(text: String) {
        savedStateHandle[JOB_TEXT_KEY] = text.take(MAX_JOB_TEXT_LENGTH)
    }

    fun onAnalyze() {
        val text = jobText.value
        if (session.value !is AnalysisSession.Idle || !text.isAnalyzable()) return
        viewModelScope.launch {
            session.value = AnalysisSession.Analyzing
            val analysis = analyze(text)
            session.value = analysis?.let(AnalysisSession.Ready::of) ?: AnalysisSession.Idle
        }
    }

    fun onEditJobText() {
        if (session.value is AnalysisSession.Ready) session.value = AnalysisSession.Idle
    }

    fun onTitleChange(title: String) = updateReady { it.copy(title = title) }

    fun onCompanyChange(company: String) = updateReady { it.copy(company = company) }

    fun onTogglePrepPlan(requirementId: String) = updateReady { it.withPrepToggled(requirementId) }

    fun onSubmitEvidence(requirementId: String, statement: String) {
        val ready = session.value as? AnalysisSession.Ready ?: return
        val requirement = ready.findRequirement(requirementId) ?: return
        val trimmed = statement.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            addUserStatedFact(requirement, trimmed)
            val fresh = analyze(ready.analysis.job.rawText) ?: return@launch
            updateReady { it.withFreshAnalysis(fresh) }
        }
    }

    fun onSave() {
        val ready = session.value as? AnalysisSession.Ready ?: return
        if (ready.title.isBlank()) return
        viewModelScope.launch {
            session.value = AnalysisSession.Saving
            val profile = currentProfile()
            if (profile == null) {
                session.value = ready
                return@launch
            }
            val applicationId = withContext(computeDispatcher) {
                createApplication(profile, ready.withEditedJob())
            }
            savePrepPlan(applicationId, ready)
            session.value = AnalysisSession.Saved(applicationId)
        }
    }

    fun onNavigationConsumed() {
        if (session.value !is AnalysisSession.Saved) return
        savedStateHandle[JOB_TEXT_KEY] = ""
        session.value = AnalysisSession.Idle
    }

    private fun updateReady(transform: (AnalysisSession.Ready) -> AnalysisSession.Ready) {
        session.update { current ->
            if (current is AnalysisSession.Ready) transform(current) else current
        }
    }

    private suspend fun currentProfile(): CandidateProfile? = profileRepository.observeProfile().first()

    private suspend fun analyze(rawJobText: String): JobAnalysisResult? {
        val profile = currentProfile() ?: return null
        return withContext(computeDispatcher) { analyzeJob(profile, rawJobText) }
    }

    private suspend fun savePrepPlan(applicationId: String, ready: AnalysisSession.Ready) {
        val notes = prepPlanNotes(ready.prepRequirements())
        if (notes.isNotEmpty()) applicationRepository.updateNotes(applicationId, notes)
    }

    private fun buildUiState(
        profile: CandidateProfile?,
        jobText: String,
        session: AnalysisSession,
    ): AnalysisUiState {
        if (profile == null || profile.entries.none { it.isConfirmed }) return AnalysisUiState.NoProfile
        return when (session) {
            AnalysisSession.Idle -> AnalysisUiState.Input(jobText, jobText.isAnalyzable())
            AnalysisSession.Analyzing -> AnalysisUiState.Analyzing
            is AnalysisSession.Ready -> session.toResultState(profile)
            AnalysisSession.Saving -> AnalysisUiState.Saving
            is AnalysisSession.Saved -> AnalysisUiState.Saved(session.applicationId)
        }
    }

    private fun String.isAnalyzable(): Boolean = trim().length >= MIN_JOB_TEXT_LENGTH
}

private const val JOB_TEXT_KEY = "jobText"
