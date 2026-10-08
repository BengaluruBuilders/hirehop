package com.tailormyresume.core.domain.prep

import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.model.CandidateProfile
import javax.inject.Inject

class GeneratePrepQuestionsUseCase @Inject constructor() : PrepQuestionSource {
    override suspend operator fun invoke(
        analysis: JobAnalysisResult,
        profile: CandidateProfile,
        limit: Int,
    ): List<PrepQuestion> = PrepQuestionGenerator.generate(analysis, profile, limit)
}
