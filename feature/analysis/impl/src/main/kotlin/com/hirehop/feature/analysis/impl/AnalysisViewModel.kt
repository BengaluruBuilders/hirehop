package com.hirehop.feature.analysis.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.common.network.Dispatcher
import com.hirehop.core.common.network.HhDispatchers
import com.hirehop.core.common.network.di.ApplicationScope
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.AddUserStatedFactUseCase
import com.hirehop.core.domain.AnalyzeJobUseCase
import com.hirehop.core.domain.CreateApplicationUseCase
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.model.CandidateProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
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
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    private val jobText = savedStateHandle.getStateFlow(JOB_TEXT_KEY, "")
    private val session = MutableStateFlow<AnalysisSession>(AnalysisSession.Idle())
    private val error = MutableStateFlow<AnalysisError?>(null)

    val uiState: StateFlow<AnalysisUiState> = combine(
        profileRepository.observeProfile(),
        jobText,
        session,
        error,
        ::buildUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalysisUiState.Loading,
    )

    fun onJobTextChange(text: String) {
        savedStateHandle[JOB_TEXT_KEY] = text.take(MAX_JOB_TEXT_LENGTH)
    }

    fun onErrorShown() {
        error.value = null
    }

    fun onAnalyze() {
        val text = jobText.value
        val idle = session.value as? AnalysisSession.Idle ?: return
        if (!text.isAnalyzable()) return
        viewModelScope.launch {
            session.value = AnalysisSession.Analyzing
            attempt { analyze(text) }.fold(
                onSuccess = { analysis -> session.value = analysis?.let(idle::resume) ?: AnalysisSession.Idle() },
                onFailure = { fail(idle, AnalysisError.AnalyzeFailed) },
            )
        }
    }

    fun onEditJobText() {
        val ready = session.value as? AnalysisSession.Ready ?: return
        session.value = AnalysisSession.Idle(previous = ready)
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
            attempt {
                addUserStatedFact(requirement, trimmed)
                analyze(ready.analysis.job.rawText)
            }.fold(
                onSuccess = { fresh -> if (fresh != null) updateReady { it.withFreshAnalysis(fresh) } },
                onFailure = { error.value = AnalysisError.AddEvidenceFailed },
            )
        }
    }

    fun onSave() {
        val ready = session.value as? AnalysisSession.Ready ?: return
        if (ready.title.isBlank()) return
        session.value = AnalysisSession.Saving
        applicationScope.launch {
            attempt { store(ready) }.fold(
                onSuccess = { applicationId ->
                    session.value = applicationId?.let(AnalysisSession::Saved) ?: ready
                },
                onFailure = { fail(ready, AnalysisError.SaveFailed) },
            )
        }
    }

    fun onNavigationConsumed() {
        if (session.value !is AnalysisSession.Saved) return
        savedStateHandle[JOB_TEXT_KEY] = ""
        session.value = AnalysisSession.Idle()
    }

    private fun fail(restoreTo: AnalysisSession, reason: AnalysisError) {
        session.value = restoreTo
        error.value = reason
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

    private suspend fun store(ready: AnalysisSession.Ready): String? {
        val profile = currentProfile() ?: return null
        val applicationId = withContext(computeDispatcher) {
            createApplication(profile, ready.withEditedJob())
        }
        val notes = prepPlanNotes(ready.prepRequirements())
        if (notes.isNotEmpty()) applicationRepository.updateNotes(applicationId, notes)
        return applicationId
    }

    private fun buildUiState(
        profile: CandidateProfile?,
        jobText: String,
        session: AnalysisSession,
        error: AnalysisError?,
    ): AnalysisUiState {
        if (profile == null || profile.entries.none { it.isConfirmed }) return AnalysisUiState.NoProfile
        return when (session) {
            is AnalysisSession.Idle -> AnalysisUiState.Input(jobText, jobText.isAnalyzable(), error)
            AnalysisSession.Analyzing -> AnalysisUiState.Analyzing
            is AnalysisSession.Ready -> session.toResultState(profile, error)
            AnalysisSession.Saving -> AnalysisUiState.Saving
            is AnalysisSession.Saved -> AnalysisUiState.Saved(session.applicationId)
        }
    }

    private fun String.isAnalyzable(): Boolean = trim().length >= MIN_JOB_TEXT_LENGTH
}

private suspend inline fun <T> attempt(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (failure: Exception) {
    Result.failure(failure)
}

private const val JOB_TEXT_KEY = "jobText"
