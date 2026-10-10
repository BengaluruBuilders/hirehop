package com.tailormyresume.core.domain

import com.tailormyresume.core.domain.offline.SkillLexicon
import com.tailormyresume.core.domain.offline.TextTokens
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.RequirementType

fun keywordsStatedIn(requirement: JobRequirement, statement: String): List<String> {
    val statedCanonicals = SkillLexicon.canonicalsIn(statement).toSet()
    val statedStems = TextTokens.stems(statement)
    return requirement.keywords
        .filter { keyword -> isStated(keyword, statedCanonicals, statedStems) }
        .map(SkillLexicon::displayName)
}

private fun isStated(keyword: String, statedCanonicals: Set<String>, statedStems: Set<String>): Boolean =
    SkillLexicon.normalise(keyword)?.let { it in statedCanonicals } ?: (TextTokens.stem(keyword) in statedStems)

fun isNamedSkillKeyword(requirement: JobRequirement, keyword: String): Boolean {
    val isSkill = requirement.type == RequirementType.SKILL ||
        requirement.type == RequirementType.TOOL ||
        SkillLexicon.normalise(keyword) != null
    return isSkill &&
        SkillLexicon.displayName(keyword) in keywordsStatedIn(requirement, requirement.text)
}

fun isKnownSkill(term: String): Boolean = SkillLexicon.normalise(term) != null

fun displayKeywords(requirement: JobRequirement): List<String> =
    requirement.keywords.map(SkillLexicon::displayName)
