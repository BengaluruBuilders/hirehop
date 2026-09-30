package com.hirehop.core.domain.offline

internal object SensitiveContent {
    private val bareLabels = listOf(
        "date of birth", "birth ?date", "dob", "d\\.o\\.b\\.?", "born", "marital status", "religion",
        "nationality", "father", "mother", "blood group", "caste", "aadhaar", "aadhar", "signature",
        "s/o", "d/o", "w/o", "passport",
    )
    private val dashOrColonLabels = listOf("gender", "sex", "place", "signed", "place of birth")
    private val colonOnlyLabels = listOf("age", "category", "pan(?: no\\.?| number)?", "photo", "sub[- ]?caste")
    private val sensitiveSegment = Regex(
        "^\\W*(?:(?:${bareLabels.joinToString("|")})\\b|" +
            "(?:${dashOrColonLabels.joinToString("|")})\\s*[:\\-–]|" +
            "(?:${colonOnlyLabels.joinToString("|")})\\s*:)",
        RegexOption.IGNORE_CASE,
    )
    private val declarationStart = Regex("^\\W*i\\s+hereby\\s+declare", RegexOption.IGNORE_CASE)
    private val segmentSeparator = Regex("\\s*\\|\\s*|\\t+|\\s{2,}")

    fun clean(line: String): String? {
        val segments = line.split(segmentSeparator)
        val separators = segmentSeparator.findAll(line).map { it.value }.toList()
        val kept = StringBuilder()
        segments.forEachIndexed { index, segment ->
            if (sensitiveSegment.containsMatchIn(segment) || segment.isBlank()) return@forEachIndexed
            if (kept.isNotEmpty()) kept.append(joinerFor(separators.getOrNull(index - 1)))
            kept.append(segment)
        }
        return kept.toString().trim().ifEmpty { null }
    }

    fun startsDeclaration(line: String): Boolean = declarationStart.containsMatchIn(line)

    private fun joinerFor(separator: String?): String =
        if (separator != null && separator.contains('|')) " | " else "  "
}
