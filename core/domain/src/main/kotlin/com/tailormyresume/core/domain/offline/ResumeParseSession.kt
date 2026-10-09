package com.tailormyresume.core.domain.offline

import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.ProfileEntry

internal class ResumeParseSession {
    private var section = ResumeSection.PREAMBLE
    private val preamble = mutableListOf<String>()
    private val skills = LinkedHashMap<String, String>()
    private val entryParsers = LinkedHashMap<EntryCategory, EntrySectionParser>()

    val preambleLines: List<String> get() = preamble

    val collectedSkills: List<String> get() = skills.values.toList()

    fun consume(rawLine: String) {
        val line = SensitiveContent.clean(rawLine) ?: return
        if (SensitiveContent.startsDeclaration(line)) {
            section = ResumeSection.IGNORED
            return
        }
        val header = ResumeHeaders.detect(line, section)
        if (header == null) {
            route(line)
            return
        }
        if (header.switchesSection) section = header.section
        if (header.remainder.isNotBlank() && header.switchesSection) route(header.remainder)
    }

    fun buildEntries(keptEntries: Int = 0): List<ProfileEntry> {
        var counter = 0
        var draftsAfter = entryParsers.values.sumOf { it.entryCount }
        val built = mutableListOf<ProfileEntry>()
        entryParsers.values.forEach { parser ->
            draftsAfter -= parser.entryCount
            built += parser.build({ "entry-${++counter}" }, keptEntries + built.size, draftsAfter)
        }
        return built
    }

    private fun route(line: String) {
        when (section) {
            ResumeSection.PREAMBLE -> preamble += line
            ResumeSection.IGNORED -> Unit
            ResumeSection.SKILLS -> SkillsLineParser.parse(line).forEach { skills.putIfAbsent(it.lowercase(), it) }
            else -> entryParserFor(section)?.accept(line)
        }
    }

    private fun entryParserFor(current: ResumeSection): EntrySectionParser? {
        val category = categoryOf(current) ?: return null
        return entryParsers.getOrPut(category) { EntrySectionParser(category) }
    }

    private fun categoryOf(section: ResumeSection): EntryCategory? = when (section) {
        ResumeSection.EDUCATION -> EntryCategory.EDUCATION
        ResumeSection.EXPERIENCE -> EntryCategory.EXPERIENCE
        ResumeSection.PROJECT -> EntryCategory.PROJECT
        ResumeSection.CERTIFICATION -> EntryCategory.CERTIFICATION
        ResumeSection.ACHIEVEMENT -> EntryCategory.ACHIEVEMENT
        ResumeSection.PREAMBLE, ResumeSection.SKILLS, ResumeSection.IGNORED -> null
    }
}
