package com.tailormyresume.app.ai

import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.coverletter.CoverLetterDraft
import com.tailormyresume.core.domain.coverletter.CoverLetterSource
import com.tailormyresume.core.domain.coverletter.GenerateCoverLetterUseCase
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.JobDescription
import javax.inject.Inject

class GuardedCoverLetterSource @Inject constructor(
    private val remote: RemoteCoverLetterSource,
    private val offline: GenerateCoverLetterUseCase,
    private val previewMode: DebugPreviewMode,
) : CoverLetterSource {
    private val active: CoverLetterSource get() = if (previewMode.active) offline else remote

    override val choosesEvidence: Boolean get() = active.choosesEvidence

    override suspend fun invoke(
        candidate: CandidateProfile,
        job: JobDescription,
        analysis: JobAnalysisResult,
        maxEvidence: Int,
    ): CoverLetterDraft = active(candidate, job, analysis, maxEvidence)
}
