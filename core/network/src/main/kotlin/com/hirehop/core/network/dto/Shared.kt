package com.hirehop.core.network.dto

import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import kotlinx.serialization.Serializable

@Serializable
data class BulletDto(val id: String, val text: String)

@Serializable
data class FactEntryDto(
    val id: String,
    val category: EntryCategory,
    val title: String,
    val organization: String,
    val startDate: String,
    val endDate: String,
    val source: FactSource,
    val bullets: List<BulletDto>,
)

@Serializable
data class ProfileFactsDto(val skills: List<String>, val entries: List<FactEntryDto>)

@Serializable
data class RequirementDto(
    val id: String,
    val text: String,
    val type: RequirementType,
    val priority: RequirementPriority,
    val keywords: List<String>,
)

@Serializable
data class JobDto(val title: String, val company: String, val requirements: List<RequirementDto>)

@Serializable
data class MatchDto(val requirementId: String, val status: MatchStatus, val evidenceIds: List<String>)
