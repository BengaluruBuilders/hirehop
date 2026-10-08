package com.tailormyresume.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CoverLetterRequest(val job: JobDto, val matches: List<MatchDto>, val profile: ProfileFactsDto)

@Serializable
enum class ParagraphRole { OPENING, EVIDENCE, CLOSING }

@Serializable
data class LetterParagraphDto(val role: ParagraphRole, val text: String, val sourceIds: List<String>)

@Serializable
data class LetterDto(val greeting: String, val paragraphs: List<LetterParagraphDto>, val wordCount: Int)

@Serializable
data class CoverLetterResponse(val generationId: String, val letter: LetterDto)
