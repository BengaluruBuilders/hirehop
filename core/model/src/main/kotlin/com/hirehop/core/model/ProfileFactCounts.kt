package com.hirehop.core.model

data class ProfileFactCounts(
    val total: Int,
    val confirmed: Int,
    val userStated: Int,
)

fun CandidateProfile.factCounts(): ProfileFactCounts = ProfileFactCounts(
    total = skills.size + entries.size,
    confirmed = skills.size + entries.count { it.isConfirmed && it.source != FactSource.USER_STATED },
    userStated = entries.count { it.isConfirmed && it.source == FactSource.USER_STATED },
)
