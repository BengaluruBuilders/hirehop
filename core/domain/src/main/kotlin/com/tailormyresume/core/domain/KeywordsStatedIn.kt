package com.tailormyresume.core.domain

import com.tailormyresume.core.domain.offline.SkillLexicon
import com.tailormyresume.core.domain.offline.TextTokens
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.RequirementType

fun keywordsStatedIn(requirement: JobRequirement, statement: String): List<String> {
    val statedTerms = SkillLexicon.canonicalsIn(statement).toSet()
    val statedStems = TextTokens.stems(statement)
    return requirement.keywords
        .filter { keyword -> isStated(keyword, statedTerms, statedStems) }
        .map(SkillLexicon::displayName)
}

private fun isStated(keyword: String, statedTerms: Set<String>, statedStems: Set<String>): Boolean =
    if (SkillLexicon.isKnown(keyword)) keyword in statedTerms else TextTokens.stem(keyword) in statedStems

fun isNamedSkillKeyword(requirement: JobRequirement, keyword: String): Boolean =
    requirement.type == RequirementType.SKILL ||
        requirement.type == RequirementType.TOOL ||
        SkillLexicon.normalise(keyword)?.let { it in SkillLexicon.canonicalsIn(requirement.text) } == true

fun isKnownSkill(term: String): Boolean = SkillLexicon.normalise(term) != null

fun displayKeywords(requirement: JobRequirement): List<String> =
    requirement.keywords.map(SkillLexicon::displayName)
