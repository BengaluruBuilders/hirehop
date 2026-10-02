package com.hirehop.core.model

data class KeptJobDescription(
    val text: String,
    val company: String,
    val role: String,
) {
    val draftKey: String get() = DRAFT_KEY_PREFIX + text.hashCode()

    private companion object {
        const val DRAFT_KEY_PREFIX = "analysis-draft-"
    }
}
