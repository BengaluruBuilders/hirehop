package com.tailormyresume.core.model

data class KeptJobDescription(
    val text: String,
    val company: String,
    val role: String,
    val companyIsPrefill: Boolean = false,
    val roleIsPrefill: Boolean = false,
) {
    val draftKey: String get() = DRAFT_KEY_PREFIX + text.hashCode()

    fun resolvedTitle(analysed: String): String = role.ifBlank { analysed }

    fun resolvedCompany(analysed: String): String = company.ifBlank { analysed }

    private companion object {
        const val DRAFT_KEY_PREFIX = "analysis-draft-"
    }
}
