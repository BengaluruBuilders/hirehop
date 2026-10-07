package com.hirehop.core.domain

import com.hirehop.core.model.CandidateProfile
import javax.inject.Inject

class AnalyzeJobUseCase @Inject constructor(private val source: JobAnalysisSource) {
    suspend operator fun invoke(profile: CandidateProfile, rawJobText: String): JobAnalysisResult =
        source.analyse(profile, rawJobText)
}
