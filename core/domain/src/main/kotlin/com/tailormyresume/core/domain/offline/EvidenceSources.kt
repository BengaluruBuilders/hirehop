package com.tailormyresume.core.domain.offline

import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.confirmedWithinLimits

internal data class EvidenceSource(val id: String, val index: EvidenceIndex)

internal object EvidenceSources {
    const val SKILL_ID_PREFIX = "skill:"

    fun of(profile: CandidateProfile): List<EvidenceSource> {
        val sendable = profile.confirmedWithinLimits()
        val fromEntries = sendable.entries.flatMap { entry ->
            listOf(EvidenceSource(entry.id, EvidenceIndex.ofText(entry.title))) +
                entry.bullets.map { EvidenceSource(it.id, EvidenceIndex.ofText(it.text)) }
        }
        val fromSkills = sendable.skills.map { EvidenceSource("$SKILL_ID_PREFIX$it", EvidenceIndex.ofSkill(it)) }
        return fromEntries + fromSkills
    }
}
