package com.tailormyresume.core.domain.profile

import com.tailormyresume.core.model.CandidateProfile

data class RequiredGap(val entryId: String, val title: String)

object RequiredGaps {
    fun of(profile: CandidateProfile): List<RequiredGap> = emptyList()
}
