package com.hirehop.feature.applications.impl

import androidx.compose.runtime.Immutable
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.RequirementPriority
import kotlin.time.Instant

@Immutable
data class WorkspaceGapCounts(
    val met: Int,
    val partial: Int,
    val gap: Int,
)

@Immutable
data class WorkspacePrepTask(
    val id: String,
    val requirementText: String,
    val priority: RequirementPriority,
    val isDone: Boolean,
    val isOverflowOpen: Boolean,
)

sealed interface WorkspaceResume {
    data object Absent : WorkspaceResume

    data object NotExported : WorkspaceResume

    data class Exported(val fileName: String) : WorkspaceResume
}

enum class WorkspaceNotesState { Idle, Saving, SavedJustNow }

@Immutable
data class WorkspaceDeleteScope(
    val hasJobDescription: Boolean,
    val hasGapAnalysis: Boolean,
    val hasTailoredResume: Boolean,
    val hasCoverLetter: Boolean,
    val hasNotes: Boolean,
    val prepTaskCount: Int,
    val prepQuestionCount: Int,
    val profileFactCount: Int,
)

@Immutable
data class WorkspaceMessage(
    val text: WorkspaceMessageText,
)

enum class WorkspaceMessageText { ReportedInaccurate }

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
        val isJobDescriptionExpanded: Boolean,
        val coverage: KeywordCoverage?,
        val gapCounts: WorkspaceGapCounts?,
        val resume: WorkspaceResume,
        val reviewProgress: ReviewProgress?,
        val prepTasks: List<WorkspacePrepTask>,
        val prepQuestionCount: Int,
        val hasCoverLetter: Boolean,
        val notes: String,
        val notesState: WorkspaceNotesState,
        val isNotesFocused: Boolean,
        val isMoreOpen: Boolean,
        val statusSheet: ApplicationStatusSheetState?,
        val isDeleteDialogVisible: Boolean,
        val deleteScope: WorkspaceDeleteScope?,
        val message: WorkspaceMessage?,
    ) : ApplicationDetailUiState
}
