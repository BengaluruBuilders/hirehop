package com.tailormyresume.core.domain.offline

internal data class NumberToken(val raw: String, val key: String)

internal object NumberClaims {
    private val ignoreCase = setOf(RegexOption.IGNORE_CASE)
    private val digitPattern = Regex(
        "(?<![\\p{L}\\p{N}])(\\d++(?:,\\d++)*(?:\\.\\d++)?)(\\+)?" +
            "(?:\\s?(%|percent\\b|lakhs?\\b|crores?\\b|million\\b|billion\\b|thousand\\b|hundred\\b|k\\b|x\\b)|([A-Za-z]{1,3})(?![A-Za-z]))?",
        ignoreCase,
    )
    private val wordPattern = Regex(
        "\\b(one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|" +
            "seventeen|eighteen|nineteen|twenty|dozens?|hundreds?|thousands?|lakhs?|crores?|millions?|billions?|" +
            "double[sd]?|triple[sd]?|twice|thrice)\\b",
        ignoreCase,
    )
    private val qualifierBefore = Regex(
        "(over|more than|under|less than|nearly|almost|about|around|approximately|up to)\\s+$|([~<>])\\s*$",
        ignoreCase,
    )
    private val wordsWithPlural = setOf("dozens", "hundreds", "thousands", "lakhs", "crores", "millions", "billions")

    fun extract(text: String): List<NumberToken> {
        val digits = digitPattern.findAll(text).toList()
        val digitRanges = digits.map { it.range }
        val words = wordPattern.findAll(text).filter { word -> digitRanges.none { word.range.first in it } }
        return digits.map { digitToken(text, it) } + words.map { wordToken(it) }
    }

    fun unsupported(proposed: String, sourceTexts: List<String>): List<NumberToken> {
        val supported = sourceTexts.flatMap { extract(it) }.map { it.key }.toSet()
        return extract(proposed).filter { it.key !in supported }.distinctBy { it.key }
    }

    private fun digitToken(text: String, match: MatchResult): NumberToken {
        val qualifier = qualifierBefore.find(text.substring(0, match.range.first))?.value?.trim()?.lowercase().orEmpty()
        val core = match.groupValues[1].replace(",", "").removeSuffix(".0")
        val unit = normaliseUnit(match.groupValues[3].ifEmpty { match.groupValues[4] })
        val raw = listOf(qualifier, match.value.trim()).filter { it.isNotEmpty() }.joinToString(" ")
        return NumberToken(raw = raw, key = "$qualifier|$core${match.groupValues[2]}$unit")
    }

    private fun wordToken(match: MatchResult): NumberToken {
        val lower = match.value.lowercase()
        val normalised = if (lower in wordsWithPlural) lower.removeSuffix("s") else lower
        return NumberToken(raw = match.value, key = "word|$normalised")
    }

    private fun normaliseUnit(unit: String): String = when (val lower = unit.lowercase()) {
        "percent" -> "%"
        "lakhs" -> "lakh"
        "crores", "cr" -> "crore"
        else -> lower
    }
}
