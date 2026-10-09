package com.tailormyresume.app.ai

import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.prep.GeneratePrepQuestionsUseCase
import com.tailormyresume.core.domain.prep.PrepQuestion
import com.tailormyresume.core.domain.prep.PrepQuestionSource
import com.tailormyresume.core.model.CandidateProfile
import javax.inject.Inject

class GuardedPrepQuestionSource @Inject constructor(
    private val remote: RemotePrepQuestionSource,
    private val offline: GeneratePrepQuestionsUseCase,
    private val previewMode: DebugPreviewMode,
) : PrepQuestionSource {
    override suspend fun invoke(
        analysis: JobAnalysisResult,
        profile: CandidateProfile,
        limit: Int,
    ): List<PrepQuestion> =
        if (previewMode.active) offline(analysis, profile, limit) else remote(analysis, profile, limit)
}
