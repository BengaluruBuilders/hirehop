package com.hirehop.core.domain.offline

import com.hirehop.core.model.RequirementType

internal object SkillLexicon {
    private val separators = Regex("[\\s\\-]+")

    val entries: List<LexiconEntry> = languageEntries + frameworkEntries + engineeringPracticeEntries +
        databaseEntries + analyticsEntries + financeEntries + marketingEntries + operationsEntries +
        softSkillEntries + degreeEntries + fieldOfStudyEntries

    private val entriesByCanonical: Map<String, LexiconEntry> = entries.associateBy { it.canonical }
    private val entriesByKey: Map<String, LexiconEntry> = buildKeyIndex()
    private val patterns: List<TermPattern> = entries.flatMap(TermPattern::compileAll)

    fun termsIn(text: String): List<LexiconTerm> {
        val normalisedText = text.replace('’', '\'')
        val candidates = patterns.flatMap { it.findIn(normalisedText) }
            .sortedWith(compareBy<LexiconTerm> { it.range.first }.thenByDescending { it.range.last })
        return withoutOverlaps(candidates)
    }

    fun canonicalsIn(text: String): List<String> = termsIn(text).map { it.canonical }.distinct()

    fun normalise(term: String): String? = entriesByKey[keyOf(term)]?.canonical

    fun isKnown(canonical: String): Boolean = canonical in entriesByCanonical

    fun displayName(canonical: String): String = entriesByCanonical[canonical]?.display ?: canonical

    fun typeOf(canonical: String): RequirementType? = entriesByCanonical[canonical]?.type

    fun isLooseSurface(term: LexiconTerm): Boolean {
        val entry = entriesByCanonical[term.canonical] ?: return true
        return entry.looseAliases.any { keyOf(it) == keyOf(term.surface) }
    }

    fun surfaceMatchesDisplay(term: LexiconTerm): Boolean {
        val entry = entriesByCanonical[term.canonical] ?: return true
        return keyOf(term.surface).trimEnd('s') == keyOf(entry.display).trimEnd('s')
    }

    fun withImplied(canonicals: Collection<String>): Set<String> {
        val result = canonicals.toMutableSet()
        val pending = ArrayDeque(canonicals)
        while (pending.isNotEmpty()) {
            val implied = entriesByCanonical[pending.removeFirst()]?.implies.orEmpty()
            implied.filter(result::add).forEach(pending::addLast)
        }
        return result
    }

    private fun keyOf(form: String): String = form.trim().lowercase().replace(separators, " ")

    private fun buildKeyIndex(): Map<String, LexiconEntry> {
        val index = LinkedHashMap<String, LexiconEntry>()
        entries.forEach { entry -> entry.allForms.forEach { index.putIfAbsent(keyOf(it), entry) } }
        return index
    }

    private fun withoutOverlaps(sorted: List<LexiconTerm>): List<LexiconTerm> {
        val kept = mutableListOf<LexiconTerm>()
        var lastEnd = -1
        for (term in sorted) {
            if (term.range.first > lastEnd) {
                kept += term
                lastEnd = term.range.last
            }
        }
        return kept
    }
}
