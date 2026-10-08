package com.tailormyresume.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PrepQuestionsRequest(
    val job: JobDto,
    val matches: List<MatchDto>,
    val profile: ProfileFactsDto,
    val limit: Int,
)

@Serializable
enum class PrepQuestionKind { STRENGTH, CLARIFY, GAP }

@Serializable
data class PrepQuestionDto(
    val id: String,
    val kind: PrepQuestionKind,
    val requirementId: String,
    val prompt: String,
    val why: String,
    val backingFactIds: List<String>,
    val gapAdvice: String?,
)

@Serializable
data class PrepQuestionsResponse(val generationId: String, val questions: List<PrepQuestionDto>)
