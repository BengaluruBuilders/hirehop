package com.hirehop.feature.applications.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.common.network.di.ApplicationScope
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ContentReportRepository
import com.hirehop.core.data.repository.CoverLetterRepository
import com.hirehop.core.data.repository.ExportHistoryRepository
import com.hirehop.core.data.repository.PrepPlanRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.prep.PrepQuestionSource
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.ContentReport
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.ExportRecord
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.PrepPlanItem
import com.hirehop.core.model.ReportedItemKind
import com.hirehop.core.model.factCounts
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

@HiltViewModel(assistedFactory = ApplicationDetailViewModel.Factory::class)
class ApplicationDetailViewModel @AssistedInject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val exportHistoryRepository: ExportHistoryRepository,
    private val prepQuestionSource: PrepQuestionSource,
    private val prepPlanRepository: PrepPlanRepository,
    private val contentReportRepository: ContentReportRepository,
    coverLetterRepository: CoverLetterRepository,
    private val clock: Clock,
    paymentGateway: PaymentGateway,
    connectivityMonitor: ConnectivityMonitor,
    @ApplicationScope applicationScope: CoroutineScope,
    @Assisted val applicationId: String,
) : ViewModel() {

    private val presentation = MutableStateFlow(WorkspacePresentation())

    private val scenario = MutableStateFlow(DebugScenario.defaultValue)

    private val isDeleted = MutableStateFlow(false)

    private val currentApplication = MutableStateFlow<JobApplication?>(null)

    private val eventChannel = Channel<ApplicationDetailEvent>(Channel.BUFFERED)

    val events: Flow<ApplicationDetailEvent> = eventChannel.receiveAsFlow()

    private val prepTasks = combine(
        prepPlanRepository.observeItems(applicationId),
        contentReportRepository.observeReportedIds(applicationId, ReportedItemKind.REQUIREMENT),
    ) { items, reportedIds -> items.map { item -> item.toWorkspacePrepTask(isReported = item.id in reportedIds) } }

    private val notesAutosaver = NotesAutosaver(
        scope = viewModelScope,
        flushScope = applicationScope,
        save = { notes ->
            applicationRepository.updateNotes(applicationId, notes)
            presentation.update { it.copy(notesState = WorkspaceNotesState.SavedJustNow) }
        },
    )

    private val stored = combine(
        applicationRepository.observeApplication(applicationId).onEach { latest ->
            currentApplication.value = latest
            presentation.update { it.copy(notesDraft = it.notesDraft.takeUnless { draft -> draft == latest?.notes }) }
        },
        profileRepository.observeProfile(),
        exportHistoryRepository.observeExports(applicationId),
        paymentGateway.observeEntitlement(),
        combine(prepTasks, coverLetterRepository.observeLetter(applicationId), ::Pair),
    ) { application, profile, exports, entitlement, (tasks, letter) ->
        StoredWorkspace(
            application = application,
            profile = profile,
            lastExport = exports.lastOrNull(),
            creditCount = entitlement.totalCredits,
            prepTasks = tasks,
            coverLetter = letter?.let { WorkspaceCoverLetter(wordCount = it.wordCount, writtenAt = it.writtenAt) },
        )
    }

    private val isOffline = combine(scenario, connectivityMonitor.isOnline) { activeScenario, isOnline ->
        activeScenario == DebugScenario.OFFLINE || !isOnline
    }

    val uiState: StateFlow<ApplicationDetailUiState> = combine(
        stored,
        isOffline,
        isDeleted,
        presentation,
    ) { storedState, offline, deleted, presentationState ->
        toUiState(
            stored = storedState,
            isOffline = offline,
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
                presentation.update { it.copy(isJobDescriptionExpanded = !it.isJobDescriptionExpanded) }
            }
            ApplicationWorkspaceAction.GapAnalysisToggled -> {
                presentation.update { it.copy(isGapExpanded = !it.isGapExpanded) }
            }
            is ApplicationWorkspaceAction.PrepTaskToggled -> togglePrepTask(action.id)
            is ApplicationWorkspaceAction.PrepTaskInaccuracyReported -> reportPrepTask(action.id)
            is ApplicationWorkspaceAction.NotesChanged -> {
                presentation.update { it.copy(notesState = WorkspaceNotesState.Saving, notesDraft = action.notes) }
                notesAutosaver.onNotesChanged(action.notes)
            }
            is ApplicationWorkspaceAction.NotesFocusChanged -> {
                presentation.update { it.copy(isNotesFocused = action.isFocused) }
            }
            ApplicationWorkspaceAction.ResumePreviewChosen -> Unit
            ApplicationWorkspaceAction.ResumeShareChosen -> Unit
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
        }
    }

    override fun onCleared() {
        notesAutosaver.flush()
    }

    private fun togglePrepTask(id: String) {
        val task = (uiState.value as? ApplicationDetailUiState.Ready)?.prepTasks?.firstOrNull { it.id == id } ?: return
        viewModelScope.launch { prepPlanRepository.setDone(applicationId, id, !task.isDone) }
    }

    private fun reportPrepTask(id: String) {
        viewModelScope.launch {
            contentReportRepository.report(
                ContentReport(
                    applicationId = applicationId,
                    itemKind = ReportedItemKind.REQUIREMENT,
                    itemId = id,
                    reportedAt = clock.now(),
                ),
            )
            eventChannel.send(ApplicationDetailEvent.ReportRecorded)
        }
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
            exportHistoryRepository.clearFor(applicationId)
            isDeleted.value = true
        }
    }

    private suspend fun toUiState(
        stored: StoredWorkspace,
        isOffline: Boolean,
        deleted: Boolean,
        presentation: WorkspacePresentation,
    ): ApplicationDetailUiState {
        if (deleted) return ApplicationDetailUiState.Deleted
        val application = stored.application ?: return ApplicationDetailUiState.NotFound
        val gap = application.gapAnalysis
        val prepTasks = stored.prepTasks
        val prepQuestionCount = application.prepQuestionCount(stored.profile)
        val hasCoverLetter = gap != null && stored.profile != null
        return ApplicationDetailUiState.Ready(
            jobTitle = application.job.title,
            company = application.job.company,
            status = application.status,
            updatedAt = application.updatedAt,
            isOffline = isOffline,
            jobDescriptionText = application.job.rawText,
            jobDescriptionWordCount = application.job.rawText.wordCount(),
            isJobDescriptionExpanded = presentation.isJobDescriptionExpanded,
            coverage = gap?.keywordCoverage,
            gapCounts = gap?.toWorkspaceGapCounts(),
            matches = gap?.toWorkspaceMatches().orEmpty(),
            isGapExpanded = presentation.isGapExpanded,
            resume = application.resumeState(stored.lastExport),
            reviewProgress = application.reviewProgressOrNull(),
            prepTasks = prepTasks,
            prepQuestionCount = prepQuestionCount,
            hasCoverLetter = hasCoverLetter,
            coverLetter = stored.coverLetter,
            notes = presentation.notesDraft ?: application.notes,
            notesState = presentation.notesState,
            isNotesFocused = presentation.isNotesFocused,
            isMoreOpen = presentation.isMoreOpen,
            statusSheet = presentation.statusSheet,
            isDeleteDialogVisible = presentation.isDeleteDialogVisible,
            deleteScope = WorkspaceDeleteScope(
                hasJobDescription = application.job.rawText.isNotBlank(),
                hasGapAnalysis = gap != null,
                hasTailoredResume = application.tailoredResume != null,
                hasNotes = application.notes.isNotBlank(),
                prepTaskCount = prepTasks.size,
                profileFactCount = stored.profile?.factCounts()?.total ?: 0,
                creditCount = stored.creditCount,
            ),
        )
    }

    private suspend fun JobApplication.prepQuestionCount(profile: CandidateProfile?): Int {
        val analysis = gapAnalysis ?: return 0
        if (profile == null) return 0
        return prepQuestionSource(
            analysis = JobAnalysisResult(job = job, gap = analysis),
            profile = profile,
        ).count { it.isTiedToFact }
    }

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String): ApplicationDetailViewModel
    }
}

private data class StoredWorkspace(
    val application: JobApplication?,
    val profile: CandidateProfile?,
    val lastExport: ExportRecord?,
    val creditCount: Int,
    val prepTasks: List<WorkspacePrepTask>,
    val coverLetter: WorkspaceCoverLetter?,
)

private data class WorkspacePresentation(
    val isMoreOpen: Boolean = false,
    val isJobDescriptionExpanded: Boolean = false,
    val isGapExpanded: Boolean = false,
    val isNotesFocused: Boolean = false,
    val notesState: WorkspaceNotesState = WorkspaceNotesState.Idle,
    val notesDraft: String? = null,
    val statusSheet: ApplicationStatusSheetState? = null,
    val isDeleteDialogVisible: Boolean = false,
)

private fun String.wordCount(): Int = trim().split(Regex("\\s+")).count { word -> word.isNotEmpty() }

private fun JobApplication.resumeState(lastExport: ExportRecord?): WorkspaceResume = when {
    tailoredResume == null -> WorkspaceResume.Absent
    lastExport == null -> WorkspaceResume.NotExported
    else -> WorkspaceResume.Exported(
        fileName = lastExport.fileName,
        format = lastExport.format,
        exportedAt = lastExport.exportedAt,
        pageCount = lastExport.pageCount,
        templateName = lastExport.templateName,
    )
}

private fun GapAnalysis.toWorkspaceGapCounts(): WorkspaceGapCounts = WorkspaceGapCounts(
    met = matches.count { match -> match.status == MatchStatus.MET },
    partial = matches.count { match -> match.status == MatchStatus.PARTIAL },
    gap = matches.count { match -> match.status == MatchStatus.GAP },
)

private fun GapAnalysis.toWorkspaceMatches(): List<WorkspaceMatch> = matches.map { match ->
    WorkspaceMatch(id = match.requirement.id, requirementText = match.requirement.text, status = match.status)
}

private fun PrepPlanItem.toWorkspacePrepTask(isReported: Boolean) = WorkspacePrepTask(
    id = id,
    requirementText = text,
    isDone = done,
    isReported = isReported,
)
