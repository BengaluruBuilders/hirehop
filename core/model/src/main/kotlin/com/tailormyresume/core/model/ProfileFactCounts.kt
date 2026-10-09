package com.tailormyresume.core.model

data class ProfileFactCounts(
    val total: Int,
    val confirmed: Int,
    val userStated: Int,
)

fun CandidateProfile.factCounts(): ProfileFactCounts {
    val statedSkills = skills.count(::isSkillUserStated)
    return ProfileFactCounts(
        total = skills.size + entries.size,
        confirmed = skills.size - statedSkills + entries.count { it.isConfirmed && it.source != FactSource.USER_STATED },
        userStated = statedSkills + entries.count { it.isConfirmed && it.source == FactSource.USER_STATED },
    )
}
