package com.tailormyresume.core.domain.export

import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.JobDescription

enum class ExportFileNameFormat { NAME_COMPANY_ROLE, NAME_ROLE, NAME_RESUME }

object ExportFileName {
    fun build(profile: CandidateProfile, job: JobDescription, format: ExportFileNameFormat): String = ""
}
