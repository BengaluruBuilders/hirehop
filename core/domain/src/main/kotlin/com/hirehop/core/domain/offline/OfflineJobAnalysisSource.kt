package com.hirehop.core.domain.offline

import com.hirehop.core.domain.GapMatcher
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.JobAnalysisSource
import com.hirehop.core.domain.JobDescriptionAnalyzer
import com.hirehop.core.domain.LocalAnalyzer
import com.hirehop.core.model.CandidateProfile
import javax.inject.Inject

class OfflineJobAnalysisSource @Inject constructor(
    @LocalAnalyzer private val analyzer: JobDescriptionAnalyzer,
    private val matcher: GapMatcher,
) : JobAnalysisSource {
    override suspend fun analyse(profile: CandidateProfile, rawJobText: String): JobAnalysisResult {
        val job = analyzer.analyze(rawJobText)
        return JobAnalysisResult(job = job, gap = matcher.match(profile, job))
    }
}
