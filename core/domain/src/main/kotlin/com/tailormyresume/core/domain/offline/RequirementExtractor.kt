package com.tailormyresume.core.domain.offline

import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.RequirementType

internal object RequirementExtractor {
    private const val MAX_FALLBACK_KEYWORDS = 3
    private const val MIN_FALLBACK_LENGTH = 4
    private val fallbackStopWords = setOf(
        "experience", "years", "year", "knowledge", "understanding", "familiarity", "familiar", "proficiency",
        "proficient", "degree", "graduate", "graduation", "with", "have", "should", "must", "required",
        "preferred", "strong", "good", "ability", "work", "working", "related", "relevant", "field",
        "candidate", "from", "this", "that", "will", "your", "plus", "bonus", "fresher", "freshers",
        "internship", "internships", "hands", "exposure", "pursuing", "final", "post", "qualification",
        "engineering", "proven", "track", "record", "bachelor", "master", "diploma",
    )

    fun extract(lines: List<JdLine>): List<JobRequirement> {
        val drafts = lines
            .filter { it.section != JdSection.IGNORED }
            .flatMap { line -> SentenceSplitter.split(line.text).mapNotNull { draft(it, line.section) } }
            .distinctBy { it.text.lowercase() }
        return drafts.mapIndexed { index, draft -> draft.copy(id = "req-${index + 1}") }
    }

    private fun draft(text: String, section: JdSection): JobRequirement? {
        if (RequirementCues.isNotARequirement(text)) return null
        val keywords = SkillLexicon.canonicalsIn(text).ifEmpty { fallbackKeywords(text) ?: return null }
        return JobRequirement(
            id = "",
            text = text,
            type = classify(text, keywords),
            priority = RequirementCues.priorityFor(text, section),
            keywords = keywords,
        )
    }

    private fun fallbackKeywords(text: String): List<String>? {
        if (!RequirementCues.hasFallbackCue(text)) return null
        return TextTokens.words(text)
            .filter { it.length >= MIN_FALLBACK_LENGTH && it !in fallbackStopWords && !it.all(Char::isDigit) }
            .distinct()
            .take(MAX_FALLBACK_KEYWORDS)
    }

    private fun classify(text: String, keywords: List<String>): RequirementType {
        val types = keywords.mapNotNull(SkillLexicon::typeOf)
        return when {
            RequirementType.EDUCATION in types -> RequirementType.EDUCATION
            RequirementCues.yearsOfExperience.containsMatchIn(text) -> RequirementType.EXPERIENCE
            types.isNotEmpty() -> mostFrequent(types)
            RequirementCues.hasEducationCue(text) -> RequirementType.EDUCATION
            else -> RequirementType.EXPERIENCE
        }
    }

    private fun mostFrequent(types: List<RequirementType>): RequirementType {
        val counts = types.groupingBy { it }.eachCount()
        val best = counts.values.max()
        return types.first { counts.getValue(it) == best }
    }
}
