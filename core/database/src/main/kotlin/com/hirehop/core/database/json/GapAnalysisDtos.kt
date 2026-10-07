package com.hirehop.core.database.json

import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import kotlinx.serialization.Serializable

@Serializable
data class JobRequirementDto(
    val id: String,
    val text: String,
    val type: RequirementType,
    val priority: RequirementPriority,
    val keywords: List<String>,
)

@Serializable
data class RequirementMatchDto(
    val requirement: JobRequirementDto,
    val status: MatchStatus,
    val evidenceIds: List<String>,
)

@Serializable
data class KeywordCoverageDto(
    val covered: Int,
    val total: Int,
)

@Serializable
data class GapAnalysisDto(
    val matches: List<RequirementMatchDto>,
    val keywordCoverage: KeywordCoverageDto,
    val generationId: String? = null,
)
