package com.tailormyresume.core.domain.offline

internal data class DateSpan(val start: String, val end: String, val range: IntRange)

internal object DateRangeExtractor {
    private const val MONTH =
        "(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|june?|july?|aug(?:ust)?|sep(?:t(?:ember)?)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)\\.?"
    private const val POINT = "(?:\\b$MONTH\\s*['’]?\\s*)?(?:19|20)\\d{2}"
    private const val END_POINT = "(?:$POINT|\\d{2}(?!\\d)|present|current|ongoing|till date|now)"
    private val options = setOf(RegexOption.IGNORE_CASE)
    private val range = Regex("\\(?($POINT)\\s*(?:-|–|—|\\bto\\b)\\s*($END_POINT)\\)?", options)
    private val single = Regex("\\(?(?:expected(?:\\s+in)?\\s+|graduating\\s+)?($POINT)\\)?", options)

    private val anchoredPoint = Regex("^$END_POINT$", options)
    private val anchoredStartPoint = Regex("^$POINT$", options)

    fun isPoint(text: String): Boolean = anchoredPoint.matches(text.trim())

    fun isStartPoint(text: String): Boolean = anchoredStartPoint.matches(text.trim())

    fun find(text: String): DateSpan? {
        range.find(text)?.let {
            return DateSpan(it.groupValues[1].trim(), it.groupValues[2].trim(), it.range)
        }
        return single.find(text)?.let { DateSpan("", it.groupValues[1].trim(), it.range) }
    }

    fun remove(text: String, span: DateSpan?): String =
        if (span == null) text else text.removeRange(span.range)
}
