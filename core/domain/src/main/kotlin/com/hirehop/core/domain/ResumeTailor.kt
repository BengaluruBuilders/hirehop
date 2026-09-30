package com.hirehop.core.domain

import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.TailoredResume

interface ResumeTailor {
    fun tailor(profile: CandidateProfile, job: JobDescription, gap: GapAnalysis): TailoredResume
}
