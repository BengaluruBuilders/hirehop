package com.hirehop.core.domain.prep

import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.model.CandidateProfile

interface PrepQuestionSource {
    suspend operator fun invoke(
        analysis: JobAnalysisResult,
        profile: CandidateProfile,
        limit: Int = PrepQuestionGenerator.MAX_QUESTIONS,
    ): List<PrepQuestion>
}
