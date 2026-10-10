package com.tailormyresume.core.domain

import com.tailormyresume.core.model.JobApplication

enum class ExportCheck { ALLOWED, NOT_ACCEPTED, PENDING_CHANGES }

object ExportReadiness {
    fun check(application: JobApplication): ExportCheck = ExportCheck.ALLOWED
}
