package com.tailormyresume.app.ai

import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.prep.PrepQuestion
import com.tailormyresume.core.domain.prep.PrepQuestionKind
import com.tailormyresume.core.domain.prep.PrepQuestionSource
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.network.TailorMyResumeApi
import com.tailormyresume.core.network.dto.PrepQuestionDto
import com.tailormyresume.core.network.dto.PrepQuestionsRequest
import com.tailormyresume.core.network.mapper.toDto
import com.tailormyresume.core.network.mapper.toFactsDto
import javax.inject.Inject

class RemotePrepQuestionSource @Inject constructor(private val api: TailorMyResumeApi) : PrepQuestionSource {
    override suspend fun invoke(
        analysis: JobAnalysisResult,
        profile: CandidateProfile,
        limit: Int,
    ): List<PrepQuestion> {
        if (limit <= 0) return emptyList()
        val facts = profile.toFactsDto()
        val request = PrepQuestionsRequest(
            job = analysis.job.toDto(),
            matches = analysis.gap.matches.map { it.toDto() },
            profile = facts,
            limit = limit.coerceAtMost(MAX_LIMIT),
        )
        val response = remoteAi { api.prepQuestions(request) }
        val requirementText = analysis.job.requirements.associate { it.id to it.text.trim() }
        val bulletIds = facts.entries.flatMap { entry -> entry.bullets.map { it.id } }.toSet()
        return response.questions.mapNotNull { dto ->
            val text = requirementText[dto.requirementId] ?: return@mapNotNull null
            val kind = PrepQuestionKind.valueOf(dto.kind.name)
            val fact = dto.backingFactIds.firstOrNull { it in bulletIds }
            if (kind != PrepQuestionKind.GAP && fact == null) return@mapNotNull null
            dto.toQuestion(kind, text, fact.takeIf { kind != PrepQuestionKind.GAP }, response.generationId)
        }
    }

    private fun PrepQuestionDto.toQuestion(kind: PrepQuestionKind, text: String, fact: String?, generationId: String) =
        PrepQuestion(
            id = id,
            kind = kind,
            prompt = prompt,
            requirementText = text,
            backingFactId = fact,
            why = why,
            gapAdvice = gapAdvice,
            generationId = generationId,
        )

    private companion object {
        const val MAX_LIMIT = 12
    }
}
