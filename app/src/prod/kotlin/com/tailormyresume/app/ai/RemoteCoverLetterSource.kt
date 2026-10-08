package com.tailormyresume.app.ai

import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.coverletter.CoverLetterDraft
import com.tailormyresume.core.domain.coverletter.CoverLetterSource
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.network.TailorMyResumeApi
import com.tailormyresume.core.network.dto.CoverLetterRequest
import com.tailormyresume.core.network.dto.LetterParagraphDto
import com.tailormyresume.core.network.dto.ParagraphRole
import com.tailormyresume.core.network.mapper.toDto
import com.tailormyresume.core.network.mapper.toFactsDto
import javax.inject.Inject

class RemoteCoverLetterSource @Inject constructor(private val api: TailorMyResumeApi) : CoverLetterSource {
    override suspend fun invoke(
        candidate: CandidateProfile,
        job: JobDescription,
        analysis: JobAnalysisResult,
        maxEvidence: Int,
    ): CoverLetterDraft {
        val request = CoverLetterRequest(
            job = job.toDto(),
            matches = analysis.gap.matches.map { it.toDto() },
            profile = candidate.toFactsDto(),
        )
        val response = remoteAi { api.coverLetter(request) }
        val paragraphs = response.letter.paragraphs
        val name = candidate.fullName.trim()
        return CoverLetterDraft(
            greeting = response.letter.greeting,
            openingParagraph = paragraphs.textOf(ParagraphRole.OPENING),
            evidenceParagraph = paragraphs.textOf(ParagraphRole.EVIDENCE),
            closingParagraph = listOf(paragraphs.textOf(ParagraphRole.CLOSING), if (name.isEmpty()) "" else "Yours sincerely, $name.")
                .filter { it.isNotBlank() }
                .joinToString(" "),
            generationId = response.generationId,
            citedFactIds = paragraphs.flatMap { it.sourceIds }.distinct(),
        )
    }

    private fun List<LetterParagraphDto>.textOf(role: ParagraphRole): String =
        filter { it.role == role }.joinToString(" ") { it.text.trim() }
}
