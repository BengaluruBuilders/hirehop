package com.hirehop.core.domain.coverletter

import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.JobDescription

interface CoverLetterSource {
    suspend operator fun invoke(
        candidate: CandidateProfile,
        job: JobDescription,
        analysis: JobAnalysisResult,
        maxEvidence: Int = CoverLetterComposer.DEFAULT_MAX_EVIDENCE,
    ): CoverLetterDraft
}
