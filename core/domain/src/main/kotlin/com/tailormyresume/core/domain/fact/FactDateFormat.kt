package com.tailormyresume.core.domain.fact

import com.tailormyresume.core.domain.offline.DateRangeExtractor

object FactDateFormat {
    private val openEnded = setOf("present", "current", "now", "ongoing", "till date", "to date")

    private val monthSlashYear = Regex("^(?:0?[1-9]|1[0-2])[/.-](?:19|20)\\d{2}$")

    fun isReadable(text: String): Boolean {
        val value = text.trim()
        if (value.isEmpty()) return true
        if (value.lowercase() in openEnded) return true
        if (DateRangeExtractor.isPoint(value) || monthSlashYear.matches(value)) return true
        return FreeFormDate.parse(value) != null
    }
}
