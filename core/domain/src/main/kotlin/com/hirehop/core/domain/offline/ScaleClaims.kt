package com.hirehop.core.domain.offline

internal object ScaleClaims {
    private val ordinaryCompounds = setOf("interface", "experience", "service", "side", "support", "story", "stories")
    private val pattern = Regex(
        "(?<![\\p{L}\\p{N}\\-])(team of(?:\\s+\\d[\\d,]*)?|users?|customers?|clients?|employees?|students?|members?|" +
            "people|engineers?|reports?|budgets?|sales|transactions?|requests?|downloads?|visitors?|subscribers?|" +
            "revenue|crores?|lakhs?|millions?|billions?)(?![\\p{L}\\p{N}]|-)(?=(?:\\s+(\\p{L}+))?)",
        RegexOption.IGNORE_CASE,
    )

    fun unsupported(proposed: String, sourceTexts: List<String>): List<String> {
        val supported = sourceTexts.flatMap { phrases(it) }.map { anchorOf(it) }.toSet()
        return phrases(proposed).filter { anchorOf(it) !in supported }.distinctBy { anchorOf(it) }
    }

    private fun phrases(text: String): List<String> =
        pattern.findAll(text)
            .filterNot { isOrdinaryCompound(it) }
            .map { it.groupValues[1] }
            .toList()

    private fun isOrdinaryCompound(match: MatchResult): Boolean {
        val next = match.groupValues[2].lowercase()
        return next in ordinaryCompounds
    }

    private fun anchorOf(phrase: String): String {
        val lower = phrase.lowercase()
        return when {
            lower.startsWith("team of") -> "team of"
            lower == "sales" || lower == "people" || lower == "revenue" -> lower
            lower.endsWith("s") -> lower.removeSuffix("s")
            else -> lower
        }
    }
}
