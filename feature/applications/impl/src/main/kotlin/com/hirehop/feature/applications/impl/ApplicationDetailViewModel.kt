package com.hirehop.feature.applications.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.common.network.di.ApplicationScope
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.prep.PrepQuestionSource
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.MatchStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ApplicationDetailViewModel.Factory::class)
class ApplicationDetailViewModel @AssistedInject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val prepQuestionSource: PrepQuestionSource,
    @ApplicationScope applicationScope: CoroutineScope,
    @Assisted val applicationId: String,
) : ViewModel() {

    private val presentation = MutableStateFlow(WorkspacePresentation())

    private val scenario = MutableStateFlow(DebugScenario.defaultValue)

    private val isDeleted = MutableStateFlow(false)

    private val currentApplication = MutableStateFlow<JobApplication?>(null)

    private val notesAutosaver = NotesAutosaver(
        scope = viewModelScope,
        flushScope = applicationScope,
        save = { notes ->
            applicationRepository.updateNotes(applicationId, notes)
            presentation.update { it.copy(notesState = WorkspaceNotesState.SavedJustNow) }
        },
    )

    val uiState: StateFlow<ApplicationDetailUiState> = combine(
        applicationRepository.observeApplication(applicationId).onEach { latest -> currentApplication.value = latest },
        profileRepository.observeProfile(),
        scenario,
        isDeleted,
        presentation,
    ) { application, profile, activeScenario, deleted, presentationState ->
        toUiState(
            application = application,
            profile = profile,
            isOffline = activeScenario == DebugScenario.OFFLINE,
            deleted = deleted,
            presentation = presentationState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApplicationDetailUiState.Loading,
    )

    fun onEnter(key: DebugScenario) {
        scenario.value = key
    }

    fun onAction(action: ApplicationWorkspaceAction) {
        when (action) {
            ApplicationWorkspaceAction.BackChosen -> Unit
            ApplicationWorkspaceAction.MoreChosen -> presentation.update { it.copy(isMoreOpen = true) }
            ApplicationWorkspaceAction.MoreDismissed -> presentation.update { it.copy(isMoreOpen = false) }
            ApplicationWorkspaceAction.StatusChipChosen -> openStatusSheet()
            ApplicationWorkspaceAction.StatusSheetDismissed -> presentation.update { it.copy(statusSheet = null) }
            is ApplicationWorkspaceAction.StatusChosen -> confirmStatus(action.status)
            ApplicationWorkspaceAction.JobDescriptionToggled -> {
                presentation.update { it.copy(isJobDescriptionExpanded = true) }
            }
            is ApplicationWorkspaceAction.PrepTaskToggled -> presentation.update { state ->
                state.copy(donePrepTaskIds = state.donePrepTaskIds.toggle(action.id))
            }
            is ApplicationWorkspaceAction.PrepTaskOverflowToggled -> presentation.update { state ->
                state.copy(
                    openPrepTaskOverflowId = if (state.openPrepTaskOverflowId == action.id) null else action.id,
                )
            }
            is ApplicationWorkspaceAction.PrepTaskInaccuracyReported -> presentation.update { state ->
                state.copy(
                    openPrepTaskOverflowId = null,
                    reportedPrepTaskIds = state.reportedPrepTaskIds + action.id,
                    message = WorkspaceMessage(text = WorkspaceMessageText.ReportedInaccurate),
                )
            }
            is ApplicationWorkspaceAction.NotesChanged -> {
                presentation.update { it.copy(notesState = WorkspaceNotesState.Saving) }
                notesAutosaver.onNotesChanged(action.notes)
            }
            is ApplicationWorkspaceAction.NotesFocusChanged -> {
                presentation.update { it.copy(isNotesFocused = action.isFocused) }
            }
            ApplicationWorkspaceAction.ResumePreviewChosen -> Unit
            is ApplicationWorkspaceAction.ResumeShareChosen -> Unit
            ApplicationWorkspaceAction.ResumeReviewChosen -> Unit
            ApplicationWorkspaceAction.PrepQuestionsChosen -> Unit
            ApplicationWorkspaceAction.CoverLetterChosen -> Unit
            ApplicationWorkspaceAction.DeleteChosen -> {
                presentation.update { it.copy(isMoreOpen = false, isDeleteDialogVisible = true) }
            }
            ApplicationWorkspaceAction.DeleteDismissed -> {
                presentation.update { it.copy(isDeleteDialogVisible = false) }
            }
            ApplicationWorkspaceAction.DeleteConfirmed -> deleteApplication()
            ApplicationWorkspaceAction.MessageDismissed -> presentation.update { it.copy(message = null) }
        }
    }

    override fun onCleared() {
        notesAutosaver.flush()
    }

    private fun openStatusSheet() {
        val application = currentApplication.value ?: return
        presentation.update {
            it.copy(
                statusSheet = ApplicationStatusSheetState(rowId = application.id, current = application.status),
                isMoreOpen = false,
            )
        }
    }

    private fun confirmStatus(status: ApplicationStatus) {
        val sheet = presentation.value.statusSheet ?: return
        presentation.update { it.copy(statusSheet = null) }
        viewModelScope.launch { applicationRepository.updateStatus(sheet.rowId, status) }
    }

    private fun deleteApplication() {
        notesAutosaver.discard()
        presentation.update { it.copy(isDeleteDialogVisible = false) }
        viewModelScope.launch {
            applicationRepository.deleteApplication(applicationId)
            isDeleted.value = true
        }
    }

    private suspend fun toUiState(
        application: JobApplication?,
        profile: CandidateProfile?,
        isOffline: Boolean,
        deleted: Boolean,
        presentation: WorkspacePresentation,
    ): ApplicationDetailUiState {
        if (deleted) return ApplicationDetailUiState.Deleted
        if (application == null) return ApplicationDetailUiState.NotFound
        val gap = application.gapAnalysis
        val prepTasks = gap?.toWorkspacePrepTasks(presentation).orEmpty()
        val prepQuestionCount = application.prepQuestionCount(profile)
        val hasCoverLetter = gap != null && profile != null
        return ApplicationDetailUiState.Ready(
            jobTitle = application.job.title,
            company = application.job.company,
            status = application.status,
            updatedAt = application.updatedAt,
            isOffline = isOffline,
            jobDescriptionText = application.job.rawText,
            isJobDescriptionExpanded = presentation.isJobDescriptionExpanded,
            coverage = gap?.keywordCoverage,
            gapCounts = gap?.toWorkspaceGapCounts(),
            resume = when {
                application.tailoredResume == null -> WorkspaceResume.Absent
                else -> WorkspaceResume.NotExported
            },
            reviewProgress = application.reviewProgressOrNull(),
            prepTasks = prepTasks,
            prepQuestionCount = prepQuestionCount,
            hasCoverLetter = hasCoverLetter,
            notes = application.notes,
            notesState = presentation.notesState,
            isNotesFocused = presentation.isNotesFocused,
            isMoreOpen = presentation.isMoreOpen,
            statusSheet = presentation.statusSheet,
            isDeleteDialogVisible = presentation.isDeleteDialogVisible,
            deleteScope = WorkspaceDeleteScope(
                hasJobDescription = application.job.rawText.isNotBlank(),
                hasGapAnalysis = gap != null,
                hasTailoredResume = application.tailoredResume != null,
                hasCoverLetter = hasCoverLetter,
                hasNotes = application.notes.isNotBlank(),
                prepTaskCount = prepTasks.size,
                prepQuestionCount = prepQuestionCount,
                profileFactCount = profile?.entries?.size ?: 0,
            ),
            message = presentation.message,
        )
    }

    private suspend fun JobApplication.prepQuestionCount(profile: CandidateProfile?): Int {
        val analysis = gapAnalysis ?: return 0
        if (profile == null) return 0
        return prepQuestionSource(
            analysis = JobAnalysisResult(job = job, gap = analysis),
            profile = profile,
        ).size
    }

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String): ApplicationDetailViewModel
    }
}

private data class WorkspacePresentation(
    val isMoreOpen: Boolean = false,
    val isJobDescriptionExpanded: Boolean = false,
    val isNotesFocused: Boolean = false,
    val notesState: WorkspaceNotesState = WorkspaceNotesState.Idle,
    val statusSheet: ApplicationStatusSheetState? = null,
    val isDeleteDialogVisible: Boolean = false,
    val donePrepTaskIds: Set<String> = emptySet(),
    val openPrepTaskOverflowId: String? = null,
    val reportedPrepTaskIds: Set<String> = emptySet(),
    val message: WorkspaceMessage? = null,
)

private fun Set<String>.toggle(id: String): Set<String> = if (id in this) this - id else this + id

private fun GapAnalysis.toWorkspaceGapCounts(): WorkspaceGapCounts = WorkspaceGapCounts(
    met = matches.count { match -> match.status == MatchStatus.MET },
    partial = matches.count { match -> match.status == MatchStatus.PARTIAL },
    gap = matches.count { match -> match.status == MatchStatus.GAP },
)

private fun GapAnalysis.toWorkspacePrepTasks(
    presentation: WorkspacePresentation,
): List<WorkspacePrepTask> = matches
    .filter { match -> match.status == MatchStatus.GAP }
    .map { match ->
        WorkspacePrepTask(
            id = match.requirement.id,
            requirementText = match.requirement.text,
            priority = match.requirement.priority,
            isDone = match.requirement.id in presentation.donePrepTaskIds,
            isOverflowOpen = match.requirement.id == presentation.openPrepTaskOverflowId,
        )
    }
