package com.tailormyresume.core.model

data class KeptJobDescription(
    val text: String,
    val company: String,
    val role: String,
    val companyIsPrefill: Boolean = false,
    val roleIsPrefill: Boolean = false,
) {
    val draftKey: String get() = DRAFT_KEY_PREFIX + text.hashCode()

    fun resolvedTitle(analysed: String): String = resolved(role, roleIsPrefill, analysed)

    fun resolvedCompany(analysed: String): String = resolved(company, companyIsPrefill, analysed)

    private fun resolved(kept: String, isPrefill: Boolean, analysed: String): String =
        if (isPrefill) analysed.ifBlank { kept } else kept.ifBlank { analysed }

    private companion object {
        const val DRAFT_KEY_PREFIX = "analysis-draft-"
    }
}
