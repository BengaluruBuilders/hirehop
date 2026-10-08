package com.tailormyresume.core.domain

import com.tailormyresume.core.model.CandidateProfile

interface JobAnalysisSource {
    suspend fun analyse(profile: CandidateProfile, rawJobText: String): JobAnalysisResult
}
