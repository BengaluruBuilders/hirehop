package com.hirehop.core.domain.offline

internal class EvidenceIndex private constructor(
    private val canonicals: Set<String>,
    private val stems: Set<String>,
) {
    fun supports(keyword: String): Boolean =
        if (SkillLexicon.isKnown(keyword)) keyword in canonicals else TextTokens.stem(keyword) in stems

    fun canonicalTerms(): Set<String> = canonicals

    companion object {
        fun ofText(text: String): EvidenceIndex {
            val terms = SkillLexicon.canonicalsIn(text)
            return EvidenceIndex(SkillLexicon.withImplied(terms), TextTokens.stems(text))
        }

        fun ofSkill(skill: String): EvidenceIndex {
            val terms = SkillLexicon.canonicalsIn(skill) + listOfNotNull(SkillLexicon.normalise(skill))
            return EvidenceIndex(SkillLexicon.withImplied(terms), TextTokens.stems(skill))
        }
    }
}
