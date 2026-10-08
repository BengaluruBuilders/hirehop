package com.tailormyresume.core.domain.offline

import com.tailormyresume.core.model.RequirementPriority

internal object RequirementCues {
    private val ignoreCase = setOf(RegexOption.IGNORE_CASE)
    private val mandatory = Regex("\\b(must|required|mandatory)\\b", ignoreCase)
    private val optional = Regex(
        "\\b(preferred|nice[- ]to[- ]have|good[- ]to[- ]have|plus|bonus|added advantage|an advantage|desirable)\\b",
        ignoreCase,
    )
    private val notARequirement = Regex(
        "\\b(not required|not mandatory|no (?:prior )?experience (?:is )?(?:required|needed|necessary)|freshers? (?:are )?(?:welcome|can apply|encouraged))\\b",
        ignoreCase,
    )
    val yearsOfExperience = Regex("\\b\\d+\\s*(?:[-–]|to)?\\s*\\d*\\+?\\s*(?:years?|yrs?)\\b", ignoreCase)
    private val experience = Regex(
        "\\b(experience|internships?|hands[- ]on|proven|familiar(?:ity)?|knowledge of|understanding of|proficien(?:t|cy)|exposure to)\\b",
        ignoreCase,
    )
    private val education = Regex(
        "\\b(degree|graduate|graduation|bachelor'?s?|master'?s?|diploma|cgpa|pursuing|final[- ]year|post[- ]?graduate|qualification|engineering|pass(?:ed|ing) out)\\b",
        ignoreCase,
    )

    private val yearsQuantity = Regex("\\b(\\d+)\\s*(?:[-–]|to)?\\s*\\d*\\+?\\s*(?:years?|yrs?)\\b", ignoreCase)

    fun minimumYears(text: String): Int? = yearsQuantity.find(text)?.groupValues?.get(1)?.toIntOrNull()

    fun isNotARequirement(text: String): Boolean = notARequirement.containsMatchIn(text)

    fun hasEducationCue(text: String): Boolean = education.containsMatchIn(text)

    fun hasFallbackCue(text: String): Boolean = experience.containsMatchIn(text) || hasEducationCue(text)

    fun priorityFor(text: String, section: JdSection): RequirementPriority = when {
        optional.containsMatchIn(text) -> RequirementPriority.NICE_TO_HAVE
        mandatory.containsMatchIn(text) -> RequirementPriority.MUST_HAVE
        section == JdSection.PREFERRED || section == JdSection.RESPONSIBILITIES -> RequirementPriority.NICE_TO_HAVE
        else -> RequirementPriority.MUST_HAVE
    }
}
