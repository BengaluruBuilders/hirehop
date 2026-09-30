package com.hirehop.feature.analysis.impl

import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementMatch
import com.hirehop.core.model.RequirementPriority

internal fun AnalysisSession.Ready.toResultState(
    profile: CandidateProfile,
    error: AnalysisError?,
): AnalysisUiState.Result {
    val resolver = EvidenceResolver(profile)
    val items = analysis.gap.matches.map { match ->
        match.toItem(resolver, isInPrepPlan = match.requirement.id in prepRequirementIds)
    }
    return AnalysisUiState.Result(
        title = title,
        company = company,
        keywordCoverage = analysis.gap.keywordCoverage,
        sections = items.toSections(),
        prepPlanCount = prepRequirements().size,
        canSave = title.isNotBlank(),
        error = error,
    )
}

private fun RequirementMatch.toItem(resolver: EvidenceResolver, isInPrepPlan: Boolean) = RequirementItem(
    requirement = requirement,
    status = status,
    evidence = if (status == MatchStatus.GAP) emptyList() else resolver.resolve(evidenceIds),
    isInPrepPlan = isInPrepPlan,
)

private fun List<RequirementItem>.toSections(): List<RequirementSection> =
    RequirementGroup.entries.mapNotNull { group ->
        filter { it.group() == group }
            .takeIf { it.isNotEmpty() }
            ?.let { RequirementSection(group, it) }
    }

private fun RequirementItem.group(): RequirementGroup = when (status) {
    MatchStatus.MET -> RequirementGroup.Met
    MatchStatus.PARTIAL -> RequirementGroup.Partial
    MatchStatus.GAP -> when (requirement.priority) {
        RequirementPriority.MUST_HAVE -> RequirementGroup.MustHaveGaps
        RequirementPriority.NICE_TO_HAVE -> RequirementGroup.NiceToHaveGaps
    }
}

internal class EvidenceResolver(profile: CandidateProfile) {
    private val bulletTextById: Map<String, String> = profile.entries
        .flatMap { it.bullets }
        .associate { it.id to it.text }

    fun resolve(evidenceIds: List<String>): List<String> = evidenceIds.mapNotNull { id ->
        if (id.startsWith(SKILL_ID_PREFIX)) {
            "Skill: ${id.removePrefix(SKILL_ID_PREFIX)}"
        } else {
            bulletTextById[id]
        }
    }

    private companion object {
        const val SKILL_ID_PREFIX = "skill:"
    }
}

internal fun prepPlanNotes(requirements: List<JobRequirement>): String =
    requirements.joinToString(separator = "\n") { "Prep: ${it.text}" }
