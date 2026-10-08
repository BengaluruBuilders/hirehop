package com.tailormyresume.core.domain.prep

internal object RequirementSlug {
    private const val MAX_LENGTH = 40
    private const val FALLBACK = "requirement"
    private val separators = Regex("[^a-z0-9]+")

    fun of(text: String): String {
        val base = text.lowercase().replace(separators, "-").trim('-')
        return base.take(MAX_LENGTH).trim('-').ifEmpty { FALLBACK }
    }
}
