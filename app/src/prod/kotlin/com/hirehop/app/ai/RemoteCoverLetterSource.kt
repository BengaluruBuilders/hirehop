package com.hirehop.app.ai

import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.coverletter.CoverLetterDraft
import com.hirehop.core.domain.coverletter.CoverLetterSource
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.JobDescription
import com.hirehop.core.network.HirehopApi
import com.hirehop.core.network.dto.CoverLetterRequest
import com.hirehop.core.network.dto.LetterParagraphDto
import com.hirehop.core.network.dto.ParagraphRole
import com.hirehop.core.network.mapper.toDto
import com.hirehop.core.network.mapper.toFactsDto
import javax.inject.Inject

class RemoteCoverLetterSource @Inject constructor(private val api: HirehopApi) : CoverLetterSource {
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
