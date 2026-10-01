package com.hirehop.core.ui

import com.hirehop.core.designsystem.component.HhApplicationStatusKind
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.JobApplication

class ApplicationStatusKindMapper {

    fun kindOf(status: ApplicationStatus): HhApplicationStatusKind =
        when (status) {
            ApplicationStatus.SAVED -> HhApplicationStatusKind.Saved
            ApplicationStatus.APPLIED -> HhApplicationStatusKind.Applied
            ApplicationStatus.INTERVIEW -> HhApplicationStatusKind.Interview
            ApplicationStatus.OFFER -> HhApplicationStatusKind.Offer
            ApplicationStatus.REJECTED -> HhApplicationStatusKind.Rejected
            ApplicationStatus.NO_RESPONSE -> HhApplicationStatusKind.NoResponse
        }

    fun kindOf(application: JobApplication): HhApplicationStatusKind = kindOf(application.status)

    fun kindsOf(applications: List<JobApplication> = emptyList()): List<HhApplicationStatusKind> =
        applications.map { kindOf(it) }
}
