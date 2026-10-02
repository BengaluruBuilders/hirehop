package com.hirehop.core.model

enum class EditType { REWORD, REORDER, SHORTEN, EMPHASISE, MERGE }

enum class BulletDecision { PENDING, ACCEPTED, REJECTED }

sealed interface GuardrailViolation {
    data object MissingSource : GuardrailViolation
    data class UnsupportedNumber(val value: String) : GuardrailViolation
    data class UnsupportedTerm(val term: String) : GuardrailViolation
    data class VerbEscalation(val from: String, val to: String) : GuardrailViolation
    data class UnsupportedScaleClaim(val phrase: String) : GuardrailViolation
}

data class TailoredBullet(
    val id: String,
    val entryId: String,
    val originalText: String,
    val proposedText: String,
    val sourceIds: List<String>,
    val editTypes: List<EditType>,
    val keywordsUsed: List<String>,
    val violations: List<GuardrailViolation>,
    val decision: BulletDecision,
)

data class TailoredResume(val bullets: List<TailoredBullet>, val entryIds: List<String>? = null)
