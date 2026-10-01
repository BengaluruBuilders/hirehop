package com.hirehop.feature.applications.impl

import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.prep.PrepQuestion
import com.hirehop.core.domain.prep.PrepQuestionKind
import com.hirehop.core.domain.prep.PrepQuestionSource
import com.hirehop.core.model.CandidateProfile

internal class TestPrepQuestionSource(
    private val answer: (JobAnalysisResult, CandidateProfile) -> Int = { _, _ -> 0 },
) : PrepQuestionSource {

    override suspend fun invoke(
        analysis: JobAnalysisResult,
        profile: CandidateProfile,
        limit: Int,
    ): List<PrepQuestion> = List(answer(analysis, profile)) { index ->
        PrepQuestion(
            id = "question-$index",
            kind = PrepQuestionKind.STRENGTH,
            prompt = "Prompt $index",
            requirementText = analysis.job.requirements.getOrNull(index)?.text.orEmpty(),
            backingFactId = null,
        )
    }
}
