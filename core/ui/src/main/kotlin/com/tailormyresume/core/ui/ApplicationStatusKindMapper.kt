package com.tailormyresume.core.ui

import com.tailormyresume.core.designsystem.component.TmrApplicationStatusKind
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.JobApplication

class ApplicationStatusKindMapper {

    fun kindOf(status: ApplicationStatus): TmrApplicationStatusKind =
        when (status) {
            ApplicationStatus.SAVED -> TmrApplicationStatusKind.Saved
            ApplicationStatus.APPLIED -> TmrApplicationStatusKind.Applied
            ApplicationStatus.INTERVIEW -> TmrApplicationStatusKind.Interview
            ApplicationStatus.OFFER -> TmrApplicationStatusKind.Offer
            ApplicationStatus.REJECTED -> TmrApplicationStatusKind.Rejected
            ApplicationStatus.NO_RESPONSE -> TmrApplicationStatusKind.NoResponse
        }

    fun kindOf(application: JobApplication): TmrApplicationStatusKind = kindOf(application.status)

    fun kindsOf(applications: List<JobApplication> = emptyList()): List<TmrApplicationStatusKind> =
        applications.map { kindOf(it) }
}
