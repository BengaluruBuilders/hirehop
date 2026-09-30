package com.hirehop.core.domain

import com.hirehop.core.domain.offline.SkillLexicon
import com.hirehop.core.domain.offline.TextTokens
import com.hirehop.core.model.JobRequirement

fun keywordsStatedIn(requirement: JobRequirement, statement: String): List<String> {
    val statedTerms = SkillLexicon.canonicalsIn(statement).toSet()
    val statedStems = TextTokens.stems(statement)
    return requirement.keywords
        .filter { keyword -> isStated(keyword, statedTerms, statedStems) }
        .map(SkillLexicon::displayName)
}

private fun isStated(keyword: String, statedTerms: Set<String>, statedStems: Set<String>): Boolean =
    if (SkillLexicon.isKnown(keyword)) keyword in statedTerms else TextTokens.stem(keyword) in statedStems
