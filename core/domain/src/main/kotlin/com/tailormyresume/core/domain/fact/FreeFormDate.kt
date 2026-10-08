package com.tailormyresume.core.domain.fact

internal data class ComparableDate(val year: Int, val month: Int, val day: Int) : Comparable<ComparableDate> {
    override fun compareTo(other: ComparableDate): Int {
        val byYear = year.compareTo(other.year)
        if (byYear != 0) return byYear
        val byMonth = month.compareTo(other.month)
        if (byMonth != 0) return byMonth
        return day.compareTo(other.day)
    }
}

internal object FreeFormDate {
    private val months = mapOf(
        "jan" to 1, "january" to 1,
        "feb" to 2, "february" to 2,
        "mar" to 3, "march" to 3,
        "apr" to 4, "april" to 4,
        "may" to 5,
        "jun" to 6, "june" to 6,
        "jul" to 7, "july" to 7,
        "aug" to 8, "august" to 8,
        "sep" to 9, "sept" to 9, "september" to 9,
        "oct" to 10, "october" to 10,
        "nov" to 11, "november" to 11,
        "dec" to 12, "december" to 12,
    )
    private val numeric = Regex("^(\\d{4})(?:[-/.](\\d{1,2}))?(?:[-/.](\\d{1,2}))?$")
    private val named = Regex("^(?:(\\d{1,2})[\\s.-]+)?([A-Za-z]{3,9})[\\s,.'-]+(\\d{4})$")

    fun parse(text: String): ComparableDate? {
        val value = text.trim()
        if (value.isEmpty()) return null
        numeric.matchEntire(value)?.let { match ->
            val year = match.groupValues[1].toInt()
            val month = match.groupValues[2].toIntOrNull() ?: 1
            val day = match.groupValues[3].toIntOrNull() ?: 1
            return of(year, month, day)
        }
        named.matchEntire(value)?.let { match ->
            val month = months[match.groupValues[2].lowercase()] ?: return null
            val year = match.groupValues[3].toInt()
            val day = match.groupValues[1].toIntOrNull() ?: 1
            return of(year, month, day)
        }
        return null
    }

    private fun of(year: Int, month: Int, day: Int): ComparableDate? =
        if (month in 1..12 && day in 1..31) ComparableDate(year, month, day) else null
}
