package com.tailormyresume.core.domain.coverletter

import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.keywordsStatedIn
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.confirmedWithinLimits

internal object EvidencePicker {
    fun pick(candidate: CandidateProfile, analysis: JobAnalysisResult, maxEvidence: Int): List<String> {
        if (maxEvidence <= 0) return emptyList()
        val confirmed = confirmedBullets(candidate)
        val chosen = LinkedHashSet<String>()
        for (match in analysis.gap.matches) {
            if (match.status != MatchStatus.MET) continue
            val text = match.evidenceIds
                .asSequence()
                .mapNotNull { id -> confirmed[id] }
                .firstOrNull { statesRequirement(it, match.requirement) }
                ?: continue
            chosen.add(text)
            if (chosen.size == maxEvidence) break
        }
        return chosen.toList()
    }

    fun statesRequirement(text: String, requirement: JobRequirement): Boolean =
        requirement.keywords.isNotEmpty() && keywordsStatedIn(requirement, text).isNotEmpty()

    private fun confirmedBullets(candidate: CandidateProfile): Map<String, String> =
        candidate.confirmedWithinLimits().entries
            .flatMap { entry -> entry.bullets.map { bullet -> bullet.id to bullet.text } }
            .toMap()
}
