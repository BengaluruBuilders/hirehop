package com.tailormyresume.core.domain.offline

internal data class TitleCompany(val title: String, val company: String, val consumedIndexes: Set<Int>)

internal object TitleCompanyExtractor {
    private val ignoreCase = setOf(RegexOption.IGNORE_CASE)
    private val titleLabel = Regex("^(?:job title|title|role|position|designation|job role|opening for)\\s*[:\\-–]\\s*(.+)$", ignoreCase)
    private val companyLabel = Regex("^(?:company(?: name)?|organi[sz]ation|employer|hiring company|client)\\s*[:\\-–]\\s*(.+)$", ignoreCase)
    private val aboutCompany = Regex("^about\\s+(?!us\\b|the\\b|company\\b|this\\b|you\\b|job\\b|role\\b)(.{2,40}?):?$", ignoreCase)
    private val companyDelimiter = Regex("\\s*[,(|]|\\s+[-–—]\\s+")
    private val sentenceEnd = Regex("\\.(?=\\s)")
    private val trailingWord = Regex("[A-Za-z]+$")
    private val abbreviationWords = setOf(
        "pvt", "ltd", "inc", "co", "llc", "plc", "pte", "st", "mr", "mrs", "ms", "dr", "jr", "sr",
    )
    private const val HEADLINE_LINES = 3

    fun extract(lines: List<String>): TitleCompany {
        val labelledTitle = firstLabelled(lines, titleLabel)
        val labelledCompany = firstLabelled(lines, companyLabel)
        val headline = if (labelledTitle == null) HeadlineParser.parse(lines.take(HEADLINE_LINES)) else null
        val about = firstLabelled(lines, aboutCompany)
        val consumed = listOfNotNull(labelledTitle?.first, labelledCompany?.first, headline?.index).toSet()
        return TitleCompany(
            title = clean(labelledTitle?.second ?: headline?.title.orEmpty()),
            company = cleanCompany(labelledCompany?.second ?: headline?.company.orEmpty().ifEmpty { about?.second.orEmpty() }),
            consumedIndexes = consumed,
        )
    }

    private fun firstLabelled(lines: List<String>, pattern: Regex): Pair<Int, String>? =
        lines.indices.firstNotNullOfOrNull { index ->
            pattern.matchEntire(lines[index])?.let { index to it.groupValues[1] }
        }

    fun clean(value: String): String = value.trim().trim('.', ':', ';', ',', '-', '–', '|').trim()

    fun cleanCompany(value: String): String {
        val head = value.split(companyDelimiter).first()
        val cut = sentenceEnd.find(head)
            ?.takeIf { match -> trailingWord.find(head.substring(0, match.range.first))?.value?.lowercase() !in abbreviationWords }
        return clean(cut?.let { head.substring(0, it.range.first) } ?: head)
    }
}
