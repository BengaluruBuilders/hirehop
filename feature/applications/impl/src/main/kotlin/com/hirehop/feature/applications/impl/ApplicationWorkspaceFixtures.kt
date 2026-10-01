package com.hirehop.feature.applications.impl

import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.RequirementPriority
import kotlin.time.Duration.Companion.hours

internal const val PREVIEW_NOTES =
    "Recruiter said round 1 is a SQL test. Revise joins and GROUP BY from the DBMS project."

internal fun previewWorkspaceReadyState(
    status: ApplicationStatus = ApplicationStatus.APPLIED,
    isOffline: Boolean = false,
    resume: WorkspaceResume = WorkspaceResume.Exported(PREVIEW_EXPORT_FILE),
    prepTasks: List<WorkspacePrepTask> = listOf(
        previewPrepTask(id = "req-agile", isDone = false, isOverflowOpen = false),
        previewPrepTask(id = "req-communication", isDone = false, isOverflowOpen = false),
    ),
    notes: String = PREVIEW_NOTES,
    notesState: WorkspaceNotesState = WorkspaceNotesState.Idle,
    isNotesFocused: Boolean = false,
    isJobDescriptionExpanded: Boolean = false,
    isMoreOpen: Boolean = false,
    isDeleteDialogVisible: Boolean = false,
    statusSheet: ApplicationStatusSheetState? = null,
    message: WorkspaceMessage? = null,
) = ApplicationDetailUiState.Ready(
    jobTitle = NORTHWIND_ROLE,
    company = NORTHWIND_COMPANY,
    status = status,
    updatedAt = PREVIEW_INSTANT - 2.hours,
    isOffline = isOffline,
    jobDescriptionText = PREVIEW_JOB_TEXT,
    isJobDescriptionExpanded = isJobDescriptionExpanded,
    coverage = KeywordCoverage(covered = 9, total = 14),
    gapCounts = WorkspaceGapCounts(met = 6, partial = 2, gap = 2),
    resume = resume,
    reviewProgress = ReviewProgress(reviewed = 2, total = 3),
    prepTasks = prepTasks,
    prepQuestionCount = 6,
    hasCoverLetter = true,
    notes = notes,
    notesState = notesState,
    isNotesFocused = isNotesFocused,
    isMoreOpen = isMoreOpen,
    statusSheet = statusSheet,
    isDeleteDialogVisible = isDeleteDialogVisible,
    deleteScope = WorkspaceDeleteScope(
        hasJobDescription = true,
        hasGapAnalysis = true,
        hasTailoredResume = true,
        hasCoverLetter = true,
        hasNotes = true,
        prepTaskCount = prepTasks.size,
        prepQuestionCount = 6,
        profileFactCount = 18,
    ),
    message = message,
)

internal fun previewPrepTask(
    id: String,
    isDone: Boolean,
    isOverflowOpen: Boolean,
) = WorkspacePrepTask(
    id = id,
    requirementText = if (id == "req-agile") "Agile delivery with JIRA" else "Clear written communication in English",
    priority = RequirementPriority.NICE_TO_HAVE,
    isDone = isDone,
    isOverflowOpen = isOverflowOpen,
)

internal const val PREVIEW_EXPORT_FILE = "Priya-Deshmukh_Northwind-GCC_Associate-Analyst.pdf"

private const val PREVIEW_JOB_TEXT =
    "Northwind GCC is hiring an Associate Analyst in Bengaluru. " +
        "You will work with the product team on reporting and analysis."
