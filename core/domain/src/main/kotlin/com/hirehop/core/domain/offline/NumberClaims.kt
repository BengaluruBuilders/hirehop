package com.hirehop.core.domain.offline

internal data class NumberToken(val raw: String, val key: String)

internal object NumberClaims {
    private val pattern = Regex(
        "(?<![\\p{L}\\p{N}])(\\d+(?:,\\d+)*(?:\\.\\d+)?)(?:\\s?(%|percent\\b|lakhs?\\b|crores?\\b|million\\b|billion\\b|k\\b|x\\b))?(?![\\p{L}])",
        RegexOption.IGNORE_CASE,
    )

    fun extract(text: String): List<NumberToken> =
        pattern.findAll(text).map { match ->
            NumberToken(raw = match.value.trim(), key = keyOf(match.groupValues[1], match.groupValues[2]))
        }.toList()

    fun unsupported(proposed: String, sourceTexts: List<String>): List<NumberToken> {
        val supported = sourceTexts.flatMap { extract(it) }.map { it.key }.toSet()
        return extract(proposed).filter { it.key !in supported }.distinctBy { it.key }
    }

    private fun keyOf(number: String, unit: String): String {
        val core = number.replace(",", "").removeSuffix(".0")
        return core + normaliseUnit(unit)
    }

    private fun normaliseUnit(unit: String): String = when (val lower = unit.lowercase()) {
        "percent" -> "%"
        "lakhs" -> "lakh"
        "crores" -> "crore"
        else -> lower
    }
}
