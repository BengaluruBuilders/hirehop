package com.hirehop.feature.applications.impl

import com.hirehop.core.model.ApplicationStatus

sealed interface ApplicationWorkspaceAction {
    data object BackChosen : ApplicationWorkspaceAction

    data object MoreChosen : ApplicationWorkspaceAction

    data object MoreDismissed : ApplicationWorkspaceAction

    data object StatusChipChosen : ApplicationWorkspaceAction

    data object StatusSheetDismissed : ApplicationWorkspaceAction

    data class StatusChosen(val status: ApplicationStatus) : ApplicationWorkspaceAction

    data object JobDescriptionToggled : ApplicationWorkspaceAction

    data object GapAnalysisToggled : ApplicationWorkspaceAction

    data class PrepTaskToggled(val id: String) : ApplicationWorkspaceAction

    data class PrepTaskInaccuracyReported(val id: String) : ApplicationWorkspaceAction

    data class NotesChanged(val notes: String) : ApplicationWorkspaceAction

    data class NotesFocusChanged(val isFocused: Boolean) : ApplicationWorkspaceAction

    data object ResumePreviewChosen : ApplicationWorkspaceAction

    data object ResumeShareChosen : ApplicationWorkspaceAction

    data object ResumeReviewChosen : ApplicationWorkspaceAction

    data object PrepQuestionsChosen : ApplicationWorkspaceAction

    data object CoverLetterChosen : ApplicationWorkspaceAction

    data object DeleteChosen : ApplicationWorkspaceAction

    data object DeleteDismissed : ApplicationWorkspaceAction

    data object DeleteConfirmed : ApplicationWorkspaceAction
}
