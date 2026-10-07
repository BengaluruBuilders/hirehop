package com.hirehop.core.domain

import com.hirehop.core.model.CandidateProfile

interface JobAnalysisSource {
    suspend fun analyse(profile: CandidateProfile, rawJobText: String): JobAnalysisResult
}
