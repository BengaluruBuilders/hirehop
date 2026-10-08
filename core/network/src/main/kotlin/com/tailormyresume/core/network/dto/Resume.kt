package com.tailormyresume.core.network.dto

import com.tailormyresume.core.model.EntryCategory
import kotlinx.serialization.Serializable

@Serializable
data class ResumeParseRequest(val text: String)

@Serializable
data class ParsedBulletDto(val ref: String, val text: String)

@Serializable
data class ParsedEntryDto(
    val ref: String,
    val category: EntryCategory,
    val title: String,
    val organization: String?,
    val startDate: String?,
    val endDate: String?,
    val bullets: List<ParsedBulletDto>,
)

@Serializable
data class ParsedProfileDto(
    val fullName: String?,
    val email: String?,
    val phone: String?,
    val headline: String?,
    val skills: List<String>,
    val entries: List<ParsedEntryDto>,
)

@Serializable
enum class SensitiveField { DATE_OF_BIRTH, PHOTO, RELIGION, CASTE, MARITAL_STATUS, GENDER }

@Serializable
data class ResumeParseResponse(
    val generationId: String,
    val profile: ParsedProfileDto,
    val droppedSensitive: List<SensitiveField>,
)
