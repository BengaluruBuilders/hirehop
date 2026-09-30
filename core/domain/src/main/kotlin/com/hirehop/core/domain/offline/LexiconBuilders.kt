package com.hirehop.core.domain.offline

import com.hirehop.core.model.RequirementType

private fun entry(
    type: RequirementType,
    display: String,
    aliases: Array<out String>,
    loose: List<String>,
    implies: List<String>,
): LexiconEntry = LexiconEntry(
    display = display,
    type = type,
    aliases = aliases.toList(),
    looseAliases = loose,
    implies = implies,
)

internal fun skill(
    display: String,
    vararg aliases: String,
    loose: List<String> = emptyList(),
    implies: List<String> = emptyList(),
): LexiconEntry = entry(RequirementType.SKILL, display, aliases, loose, implies)

internal fun tool(
    display: String,
    vararg aliases: String,
    loose: List<String> = emptyList(),
    implies: List<String> = emptyList(),
): LexiconEntry = entry(RequirementType.TOOL, display, aliases, loose, implies)

internal fun softSkill(
    display: String,
    vararg aliases: String,
    loose: List<String> = emptyList(),
): LexiconEntry = entry(RequirementType.SOFT_SKILL, display, aliases, loose, emptyList())

internal fun education(
    display: String,
    vararg aliases: String,
    loose: List<String> = emptyList(),
    implies: List<String> = emptyList(),
): LexiconEntry = entry(RequirementType.EDUCATION, display, aliases, loose, implies)
