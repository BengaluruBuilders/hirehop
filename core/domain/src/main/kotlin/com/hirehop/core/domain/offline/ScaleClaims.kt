package com.hirehop.core.domain.offline

internal object ScaleClaims {
    private val pattern = Regex(
        "\\b(team of(?:\\s+\\d[\\d,]*)?|users?|customers?|clients?|revenue|crores?|lakhs?|millions?|billions?)\\b",
        RegexOption.IGNORE_CASE,
    )

    fun unsupported(proposed: String, sourceTexts: List<String>): List<String> {
        val supported = sourceTexts.flatMap { phrases(it) }.map { anchorOf(it) }.toSet()
        return phrases(proposed).filter { anchorOf(it) !in supported }.distinctBy { anchorOf(it) }
    }

    private fun phrases(text: String): List<String> = pattern.findAll(text).map { it.value }.toList()

    private fun anchorOf(phrase: String): String {
        val lower = phrase.lowercase()
        return when {
            lower.startsWith("team of") -> "team of"
            lower.endsWith("s") -> lower.removeSuffix("s")
            else -> lower
        }
    }
}
