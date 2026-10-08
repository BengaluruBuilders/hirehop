package com.tailormyresume.core.domain.coverletter

import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.JobDescription
import javax.inject.Inject

class GenerateCoverLetterUseCase @Inject constructor() : CoverLetterSource {
    override suspend operator fun invoke(
        candidate: CandidateProfile,
        job: JobDescription,
        analysis: JobAnalysisResult,
        maxEvidence: Int,
    ): CoverLetterDraft = CoverLetterComposer.compose(candidate, job, analysis, maxEvidence)
}
