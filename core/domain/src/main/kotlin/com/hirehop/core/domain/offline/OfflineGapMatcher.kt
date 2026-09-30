package com.hirehop.core.domain.offline

import com.hirehop.core.domain.GapMatcher
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementMatch
import com.hirehop.core.model.RequirementType
import javax.inject.Inject

internal class OfflineGapMatcher @Inject constructor() : GapMatcher {
    override fun match(profile: CandidateProfile, job: JobDescription): GapAnalysis {
        val sources = EvidenceSources.of(profile)
        val matches = job.requirements.map { matchRequirement(it, sources) }
        val keywords = job.requirements.flatMap { it.keywords }.distinct()
        val covered = keywords.count { keyword -> sources.any { it.index.supports(keyword) } }
        return GapAnalysis(matches, KeywordCoverage(covered = covered, total = keywords.size))
    }

    private fun matchRequirement(requirement: JobRequirement, sources: List<EvidenceSource>): RequirementMatch {
        val supporting = sources.filter { source -> requirement.keywords.any { source.index.supports(it) } }
        return RequirementMatch(
            requirement = requirement,
            status = statusOf(requirement, sources),
            evidenceIds = supporting.map { it.id }.distinct(),
        )
    }

    private fun statusOf(requirement: JobRequirement, sources: List<EvidenceSource>): MatchStatus {
        val (degreeKeywords, otherKeywords) = requirement.keywords.partition {
            SkillLexicon.typeOf(it) == RequirementType.EDUCATION
        }
        val units = otherKeywords.map { listOf(it) } + listOfNotNull(degreeKeywords.takeIf { it.isNotEmpty() })
        val covered = units.count { alternatives ->
            alternatives.any { keyword -> sources.any { it.index.supports(keyword) } }
        }
        return when {
            units.isEmpty() || covered == 0 -> MatchStatus.GAP
            covered == units.size -> MatchStatus.MET
            else -> MatchStatus.PARTIAL
        }
    }
}
