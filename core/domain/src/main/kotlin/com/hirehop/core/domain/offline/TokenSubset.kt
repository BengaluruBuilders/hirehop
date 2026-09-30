package com.hirehop.core.domain.offline

internal object TokenSubset {
    private val joiners = setOf("and", "the", "a", "an", "of", "in", "to", "for", "with", "using")
    private val wordPattern = Regex("[\\p{L}\\p{N}]+")

    fun unsupported(proposed: String, sourceTexts: List<String>): List<String> {
        val allowed = sourceStems(sourceTexts) + aliasTargetStems(sourceTexts)
        return wordPattern.findAll(proposed)
            .map { it.value }
            .filterNot(::isExempt)
            .filter { TextTokens.stem(it) !in allowed }
            .distinctBy { TextTokens.stem(it) }
            .toList()
    }

    private fun isExempt(word: String): Boolean =
        word.lowercase() in joiners || word.all(Char::isDigit) || (word.length == 1 && word.first().isLetter())

    private fun sourceStems(sourceTexts: List<String>): Set<String> =
        sourceTexts.flatMap { TextTokens.words(it) }.map(TextTokens::stem).toSet()

    private fun aliasTargetStems(sourceTexts: List<String>): Set<String> =
        sourceTexts.flatMap { SkillLexicon.termsIn(it) }
            .filterNot(SkillLexicon::isLooseSurface)
            .flatMap { TextTokens.words(SkillLexicon.displayName(it.canonical)) }
            .map(TextTokens::stem)
            .toSet()
}
