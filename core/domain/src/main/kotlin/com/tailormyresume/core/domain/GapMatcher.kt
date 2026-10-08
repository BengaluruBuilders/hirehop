package com.tailormyresume.core.domain

import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobDescription

interface GapMatcher {
    fun match(profile: CandidateProfile, job: JobDescription): GapAnalysis
}
