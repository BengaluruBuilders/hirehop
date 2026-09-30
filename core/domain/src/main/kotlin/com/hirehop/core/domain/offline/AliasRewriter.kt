package com.hirehop.core.domain.offline

internal object AliasRewriter {
    fun rewrite(text: String, jobKeywords: Set<String>): String {
        val replacements = SkillLexicon.termsIn(text).filter { shouldReplace(it, jobKeywords) }
        return replacements.asReversed().fold(text) { current, term ->
            current.replaceRange(term.range, SkillLexicon.displayName(term.canonical))
        }
    }

    private fun shouldReplace(term: LexiconTerm, jobKeywords: Set<String>): Boolean =
        term.canonical in jobKeywords &&
            !SkillLexicon.surfaceMatchesDisplay(term) &&
            !SkillLexicon.isLooseSurface(term)
}
