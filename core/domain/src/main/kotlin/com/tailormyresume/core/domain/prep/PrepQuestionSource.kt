package com.tailormyresume.core.domain.prep

import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.model.CandidateProfile

interface PrepQuestionSource {
    suspend operator fun invoke(
        analysis: JobAnalysisResult,
        profile: CandidateProfile,
        limit: Int = PrepQuestionGenerator.MAX_QUESTIONS,
    ): List<PrepQuestion>
}
