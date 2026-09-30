package com.hirehop.core.domain.coverletter

import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.keywordsStatedIn
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.MatchStatus

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
        candidate.entries
            .filter { it.isConfirmed }
            .flatMap { entry -> entry.bullets.map { bullet -> bullet.id to bullet.text } }
            .toMap()
}
