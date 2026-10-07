package com.hirehop.core.network.dto

import com.hirehop.core.model.EditType
import com.hirehop.core.model.EntryCategory
import kotlinx.serialization.Serializable

@Serializable
data class TailoringStartRequest(
    val requestId: String,
    val applicationId: String,
    val job: JobDto,
    val matches: List<MatchDto>,
    val profile: ProfileFactsDto,
    val section: EntryCategory?,
)

@Serializable
enum class TailoringStatus { RUNNING, SUCCEEDED, FAILED }

@Serializable
enum class TailoringFailureCode { AI_PROVIDER_ERROR, QUOTA_EXCEEDED, BUDGET_EXCEEDED, INTERRUPTED }

@Serializable
enum class BulletVerification { PASSED, REPAIRED, REVERTED, UNCHANGED }

@Serializable
data class TailoredBulletDto(
    val id: String,
    val entryId: String,
    val sourceIds: List<String>,
    val proposedText: String,
    val editTypes: List<EditType>,
    val keywordsUsed: List<String>,
    val verification: BulletVerification,
)

@Serializable
data class TailoringResultDto(val generationId: String, val bullets: List<TailoredBulletDto>)

@Serializable
data class TailoringDto(
    val id: String,
    val status: TailoringStatus,
    val result: TailoringResultDto? = null,
    val failureCode: TailoringFailureCode? = null,
)

@Serializable
data class TailoringResponse(val tailoring: TailoringDto)
