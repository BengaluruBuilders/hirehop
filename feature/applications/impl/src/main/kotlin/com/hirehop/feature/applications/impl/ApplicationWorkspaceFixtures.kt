package com.hirehop.feature.applications.impl

import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.ExportFormat
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

internal const val PREVIEW_NOTES =
    "Applied on the Northwind careers page on 14 Apr. Referral asked through a senior from Saffron Retail."

internal const val PREVIEW_EXPORT_FILE = "Priya-Deshmukh_Northwind-GCC_Associate-Analyst.pdf"

internal val PREVIEW_EXPORTED_AT: Instant = Instant.fromEpochSeconds(1_776_182_400L)

private const val PREVIEW_JOB_TEXT =
    "Associate Analyst, Business Intelligence. Northwind Global Capability Centre, Bengaluru. " +
        "0 to 2 years of experience. You will work with the product team on reporting and analysis."

internal fun previewExportedResume() = WorkspaceResume.Exported(
    fileName = PREVIEW_EXPORT_FILE,
    format = ExportFormat.PDF,
    exportedAt = PREVIEW_EXPORTED_AT,
)

internal fun previewWorkspaceReadyState(
    status: ApplicationStatus = ApplicationStatus.APPLIED,
    isOffline: Boolean = false,
    resume: WorkspaceResume = previewExportedResume(),
    prepTasks: List<WorkspacePrepTask> = listOf(
        previewPrepTask(id = "req-bigquery", isDone = true),
        previewPrepTask(id = "req-agile", isDone = false),
        previewPrepTask(id = "req-python", isDone = false),
    ),
    notes: String = PREVIEW_NOTES,
    notesState: WorkspaceNotesState = WorkspaceNotesState.Idle,
    isNotesFocused: Boolean = false,
    isJobDescriptionExpanded: Boolean = false,
    isGapExpanded: Boolean = false,
    isMoreOpen: Boolean = false,
    isDeleteDialogVisible: Boolean = false,
    statusSheet: ApplicationStatusSheetState? = null,
) = ApplicationDetailUiState.Ready(
    jobTitle = NORTHWIND_ROLE,
    company = NORTHWIND_COMPANY,
    status = status,
    updatedAt = PREVIEW_INSTANT - 2.hours,
    isOffline = isOffline,
    jobDescriptionText = PREVIEW_JOB_TEXT,
    jobDescriptionWordCount = 312,
    isJobDescriptionExpanded = isJobDescriptionExpanded,
    coverage = KeywordCoverage(covered = 9, total = 14),
    gapCounts = WorkspaceGapCounts(met = 3, partial = 1, gap = 4),
    matches = previewMatches(),
    isGapExpanded = isGapExpanded,
    resume = resume,
    reviewProgress = ReviewProgress(reviewed = 2, total = 3),
    prepTasks = prepTasks,
    prepQuestionCount = 10,
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
        hasNotes = true,
        prepTaskCount = prepTasks.size,
        profileFactCount = 18,
        creditCount = 4,
    ),
)

internal fun previewPrepTask(
    id: String,
    isDone: Boolean,
    isReported: Boolean = false,
) = WorkspacePrepTask(
    id = id,
    requirementText = when (id) {
        "req-bigquery" -> "Learn the basics of BigQuery with a public dataset"
        "req-agile" -> "Read an introduction to Agile and JIRA boards"
        else -> "Practise one Python data-cleaning exercise"
    },
    isDone = isDone,
    isReported = isReported,
)

internal fun previewMatches() = listOf(
    WorkspaceMatch(id = "req-sql", requirementText = "SQL for reporting", status = MatchStatus.MET),
    WorkspaceMatch(id = "req-excel", requirementText = "Advanced Excel", status = MatchStatus.MET),
    WorkspaceMatch(id = "req-dashboards", requirementText = "Dashboards for stakeholders", status = MatchStatus.MET),
    WorkspaceMatch(id = "req-python", requirementText = "Python for data cleaning", status = MatchStatus.PARTIAL),
    WorkspaceMatch(id = "req-bigquery", requirementText = "BigQuery", status = MatchStatus.GAP),
    WorkspaceMatch(id = "req-agile", requirementText = "Agile delivery with JIRA", status = MatchStatus.GAP),
)
