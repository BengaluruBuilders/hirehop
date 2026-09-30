package com.hirehop.core.domain

import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobDescription

interface GapMatcher {
    fun match(profile: CandidateProfile, job: JobDescription): GapAnalysis
}
