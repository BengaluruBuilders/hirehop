package com.hirehop.feature.applications.impl

import com.hirehop.core.model.ApplicationStatus

sealed interface ApplicationsAction {
    data class ApplicationChosen(val id: String) : ApplicationsAction

    data object PasteJobChosen : ApplicationsAction

    data object CreditsChosen : ApplicationsAction

    data class StatusChipChosen(val id: String) : ApplicationsAction

    data object StatusSheetDismissed : ApplicationsAction

    data class StatusChosen(val status: ApplicationStatus) : ApplicationsAction

    data object StatusUndoChosen : ApplicationsAction

    data object MessageDismissed : ApplicationsAction
}
