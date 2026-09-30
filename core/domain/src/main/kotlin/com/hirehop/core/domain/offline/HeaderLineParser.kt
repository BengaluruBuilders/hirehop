package com.hirehop.core.domain.offline

import com.hirehop.core.model.EntryCategory

internal data class HeaderParts(val title: String, val organization: String, val start: String, val end: String)

internal object HeaderLineParser {
    private val organizationSplit = Regex("\\s*\\|\\s*|\\s+[–—-]\\s+|\\s+at\\s+|\\s*,\\s*")
    private val emptyBrackets = Regex("\\(\\s*\\)|\\[\\s*]")
    private val whitespace = Regex("\\s+")
    private val edgeSeparators = charArrayOf(' ', ',', '|', '-', '–', '—', ':', '(', ')', '[', ']')

    fun parseEntryLine(text: String, category: EntryCategory): HeaderParts {
        val tracksDates = category in datedCategories
        val span = if (tracksDates) DateRangeExtractor.find(text) else null
        val remaining = tidy(DateRangeExtractor.remove(text, span))
        val (title, organization) = if (category in splitCategories) splitOrganization(remaining) else remaining to ""
        return HeaderParts(title, organization, span?.start.orEmpty(), span?.end.orEmpty())
    }

    fun parseMetaLine(text: String): HeaderParts {
        val span = DateRangeExtractor.find(text)
        return HeaderParts("", tidy(DateRangeExtractor.remove(text, span)), span?.start.orEmpty(), span?.end.orEmpty())
    }

    fun isDateOnly(text: String): Boolean {
        val span = DateRangeExtractor.find(text) ?: return false
        return tidy(DateRangeExtractor.remove(text, span)).isEmpty()
    }

    private fun splitOrganization(text: String): Pair<String, String> {
        val parts = text.split(organizationSplit).map { it.trim() }.filter { it.isNotEmpty() }
        return (parts.firstOrNull() ?: "") to parts.drop(1).joinToString(", ")
    }

    private fun tidy(text: String): String =
        text.replace(emptyBrackets, " ").replace(whitespace, " ").trim(*edgeSeparators)

    private val datedCategories = setOf(EntryCategory.EDUCATION, EntryCategory.EXPERIENCE, EntryCategory.PROJECT)
    private val splitCategories = setOf(EntryCategory.EDUCATION, EntryCategory.EXPERIENCE)
}
