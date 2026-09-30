package com.hirehop.core.domain.offline

internal object ClauseEmphasiser {
    private const val MIN_PREFIX_WORDS = 2
    private val connector = Regex("\\s(using|via|through)\\s", RegexOption.IGNORE_CASE)
    private val clauseBoundary = Regex(
        "\\s(?:to|for|that|which|by|while|resulting|so)\\s|[,;]|\\.(?=\\s|$)|$",
        RegexOption.IGNORE_CASE,
    )
    private val verbAfterAnd = Regex("\\sand\\s+[a-z]+(?:ed|ing)\\b")

    fun emphasise(text: String, jobKeywords: Set<String>): String {
        val match = connector.find(text) ?: return text
        val prefix = text.substring(0, match.range.first).trim()
        val rest = text.substring(match.range.last + 1)
        val boundary = clauseBoundary.find(rest)?.range?.first ?: rest.length
        val clauseBody = rest.substring(0, boundary).trim()
        val remainder = rest.substring(boundary)
        if (verbAfterAnd.containsMatchIn(clauseBody) || !qualifies(prefix, clauseBody, jobKeywords)) return text
        val moved = "${match.groupValues[1].replaceFirstChar { it.uppercase() }} $clauseBody"
        return "$moved, ${lowercaseLeadingWord(prefix)}$remainder"
    }

    private fun qualifies(prefix: String, clause: String, jobKeywords: Set<String>): Boolean {
        val prefixWords = prefix.split(Regex("\\s+")).size
        return clause.isNotEmpty() &&
            prefixWords >= MIN_PREFIX_WORDS &&
            containsKeyword(clause, jobKeywords) &&
            !containsKeyword(prefix, jobKeywords)
    }

    private fun containsKeyword(text: String, jobKeywords: Set<String>): Boolean =
        SkillLexicon.canonicalsIn(text).any { it in jobKeywords }

    private fun lowercaseLeadingWord(text: String): String {
        val first = text.firstOrNull() ?: return text
        val second = text.getOrNull(1)
        val plainWord = first.isUpperCase() && second != null && second.isLowerCase()
        return if (plainWord) first.lowercase() + text.substring(1) else text
    }
}
