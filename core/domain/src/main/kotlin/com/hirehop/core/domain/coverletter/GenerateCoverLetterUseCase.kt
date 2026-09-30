package com.hirehop.core.domain.coverletter

import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.JobDescription
import javax.inject.Inject

class GenerateCoverLetterUseCase @Inject constructor() : CoverLetterSource {
    override suspend operator fun invoke(
        candidate: CandidateProfile,
        job: JobDescription,
        analysis: JobAnalysisResult,
        maxEvidence: Int,
    ): CoverLetterDraft = CoverLetterComposer.compose(candidate, job, analysis, maxEvidence)
}
