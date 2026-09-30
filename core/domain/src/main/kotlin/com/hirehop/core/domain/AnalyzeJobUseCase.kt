package com.hirehop.core.domain

import com.hirehop.core.model.CandidateProfile
import javax.inject.Inject

class AnalyzeJobUseCase @Inject constructor(
    private val analyzer: JobDescriptionAnalyzer,
    private val matcher: GapMatcher,
) {
    operator fun invoke(profile: CandidateProfile, rawJobText: String): JobAnalysisResult {
        val job = analyzer.analyze(rawJobText)
        return JobAnalysisResult(job = job, gap = matcher.match(profile, job))
    }
}
