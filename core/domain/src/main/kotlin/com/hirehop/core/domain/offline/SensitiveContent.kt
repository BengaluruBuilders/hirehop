package com.hirehop.core.domain.offline

internal object SensitiveContent {
    private val sensitiveSegment = Regex(
        "^\\W*(?:(?:date of birth|dob|d\\.o\\.b\\.?|marital status|religion|nationality|father'?s? name|mother'?s? name|blood group|caste)\\b|(?:gender|sex|place|passport(?: no\\.?| number)?|signature|signed)\\s*[:\\-–]|signature\\b)",
        RegexOption.IGNORE_CASE,
    )
    private val declarationStart = Regex("^\\W*i\\s+hereby\\s+declare", RegexOption.IGNORE_CASE)
    private val segmentSeparator = Regex("\\s*\\|\\s*")

    fun clean(line: String): String? {
        val kept = line.split(segmentSeparator).filterNot { sensitiveSegment.containsMatchIn(it) }
        return kept.joinToString(" | ").trim().ifEmpty { null }
    }

    fun startsDeclaration(line: String): Boolean = declarationStart.containsMatchIn(line)
}
