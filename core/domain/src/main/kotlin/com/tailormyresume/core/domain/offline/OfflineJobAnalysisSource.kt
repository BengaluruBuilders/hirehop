package com.tailormyresume.core.domain.offline

import com.tailormyresume.core.domain.GapMatcher
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.JobAnalysisSource
import com.tailormyresume.core.domain.JobDescriptionAnalyzer
import com.tailormyresume.core.domain.LocalAnalyzer
import com.tailormyresume.core.model.CandidateProfile
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
