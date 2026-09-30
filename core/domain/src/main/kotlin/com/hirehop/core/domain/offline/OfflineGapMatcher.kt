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
            status = cappedByYears(requirement, statusOf(requirement, sources)),
            evidenceIds = supporting.map { it.id }.distinct(),
        )
    }

    private fun statusOf(requirement: JobRequirement, sources: List<EvidenceSource>): MatchStatus {
        val units = unitsOf(requirement.keywords)
        val covered = units.count { alternatives ->
            alternatives.any { keyword -> sources.any { it.index.supports(keyword) } }
        }
        return when {
            units.isEmpty() || covered == 0 -> MatchStatus.GAP
            covered == units.size -> MatchStatus.MET
            else -> MatchStatus.PARTIAL
        }
    }

    private fun unitsOf(keywords: List<String>): List<List<String>> {
        val education = keywords.filter { SkillLexicon.typeOf(it) == RequirementType.EDUCATION }
        val (fields, levels) = education.partition(SkillLexicon::isFieldOfStudy)
        val others = keywords.filterNot { it in education }
        return others.map { listOf(it) } + listOfNotNull(levels.takeIf { it.isNotEmpty() }, fields.takeIf { it.isNotEmpty() })
    }

    private fun cappedByYears(requirement: JobRequirement, status: MatchStatus): MatchStatus {
        val years = RequirementCues.minimumYears(requirement.text) ?: return status
        return if (years >= 1 && status == MatchStatus.MET) MatchStatus.PARTIAL else status
    }
}
