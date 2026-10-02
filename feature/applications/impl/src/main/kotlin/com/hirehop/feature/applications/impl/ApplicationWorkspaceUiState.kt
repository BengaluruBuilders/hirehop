package com.hirehop.feature.applications.impl

import androidx.compose.runtime.Immutable
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.ExportFormat
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import kotlin.time.Instant

@Immutable
data class WorkspaceGapCounts(
    val met: Int,
    val partial: Int,
    val gap: Int,
)

@Immutable
data class WorkspaceMatch(
    val id: String,
    val requirementText: String,
    val status: MatchStatus,
)

@Immutable
data class WorkspaceCoverLetter(
    val wordCount: Int,
    val writtenAt: Instant,
)

@Immutable
data class WorkspacePrepTask(
    val id: String,
    val requirementText: String,
    val isDone: Boolean,
    val isReported: Boolean,
)

sealed interface WorkspaceResume {
    data object Absent : WorkspaceResume

    data object NotExported : WorkspaceResume

    data class Exported(
        val fileName: String,
        val format: ExportFormat,
        val exportedAt: Instant,
        val pageCount: Int? = null,
        val templateName: String? = null,
    ) : WorkspaceResume
}

enum class WorkspaceNotesState { Idle, Saving, SavedJustNow }

@Immutable
data class WorkspaceDeleteScope(
    val hasJobDescription: Boolean,
    val hasGapAnalysis: Boolean,
    val hasTailoredResume: Boolean,
    val hasNotes: Boolean,
    val prepTaskCount: Int,
    val profileFactCount: Int,
    val creditCount: Int,
)

sealed interface ApplicationDetailUiState {
    data object Loading : ApplicationDetailUiState

    data object NotFound : ApplicationDetailUiState

    data object Deleted : ApplicationDetailUiState

    data class Ready(
        val jobTitle: String,
        val company: String,
        val status: ApplicationStatus,
        val updatedAt: Instant,
        val isOffline: Boolean,
        val jobDescriptionText: String,
        val jobDescriptionWordCount: Int,
        val isJobDescriptionExpanded: Boolean,
        val coverage: KeywordCoverage?,
        val gapCounts: WorkspaceGapCounts?,
        val matches: List<WorkspaceMatch>,
        val isGapExpanded: Boolean,
        val resume: WorkspaceResume,
        val reviewProgress: ReviewProgress?,
        val prepTasks: List<WorkspacePrepTask>,
        val prepQuestionCount: Int,
        val hasCoverLetter: Boolean,
        val coverLetter: WorkspaceCoverLetter? = null,
        val notes: String,
        val notesState: WorkspaceNotesState,
        val isNotesFocused: Boolean,
        val isMoreOpen: Boolean,
        val statusSheet: ApplicationStatusSheetState?,
        val isDeleteDialogVisible: Boolean,
        val deleteScope: WorkspaceDeleteScope?,
    ) : ApplicationDetailUiState
}

sealed interface ApplicationDetailEvent {
    data object ReportRecorded : ApplicationDetailEvent
}
