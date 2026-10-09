package com.tailormyresume.feature.analysis.impl

import com.tailormyresume.core.domain.fact.FactDisplayIds
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.isSkillUserStated

internal fun List<RequirementMatch>.toSections(
    profile: CandidateProfile,
    prepRequirementIds: Set<String>,
    reportedRequirementIds: Set<String>,
): List<RequirementSection> {
    val resolver = EvidenceResolver(profile)
    val items = map { match ->
        match.toItem(
            resolver = resolver,
            isInPrepPlan = match.requirement.id in prepRequirementIds,
            isReported = match.requirement.id in reportedRequirementIds,
        )
    }
    return RequirementGroup.entries.mapNotNull { group ->
        items.filter { it.group() == group }
            .takeIf { it.isNotEmpty() }
            ?.let { RequirementSection(group, it) }
    }
}

private fun RequirementMatch.toItem(
    resolver: EvidenceResolver,
    isInPrepPlan: Boolean,
    isReported: Boolean,
): RequirementItem {
    val isGap = status == MatchStatus.GAP
    return RequirementItem(
        requirement = requirement,
        status = status,
        skills = if (isGap) emptyList() else resolver.skillsOf(evidenceIds),
        isInPrepPlan = isInPrepPlan,
        isReported = isReported,
        factRefs = if (isGap) emptyList() else resolver.factRefsOf(evidenceIds),
        userStatedSkills = if (isGap) emptyList() else resolver.userStatedSkillsOf(evidenceIds),
    )
}

private fun RequirementItem.group(): RequirementGroup = when (status) {
    MatchStatus.MET -> RequirementGroup.Met
    MatchStatus.PARTIAL -> RequirementGroup.Partial
    MatchStatus.GAP -> when (requirement.priority) {
        RequirementPriority.MUST_HAVE -> RequirementGroup.MustHaveGaps
        RequirementPriority.NICE_TO_HAVE -> RequirementGroup.NiceToHaveGaps
    }
}

internal class EvidenceResolver(private val profile: CandidateProfile) {
    private val bulletTextById: Map<String, String> = profile.entries
        .flatMap { it.bullets }
        .associate { it.id to it.text }

    private val entryByBulletId: Map<String, ProfileEntry> = profile.entries
        .flatMap { entry -> entry.bullets.map { it.id to entry } }
        .toMap()

    private val entryById: Map<String, ProfileEntry> = profile.entries.associateBy { it.id }

    fun skillsOf(evidenceIds: List<String>): List<String> = evidenceIds
        .filter { it.startsWith(SKILL_ID_PREFIX) }
        .map { it.removePrefix(SKILL_ID_PREFIX) }

    fun userStatedSkillsOf(evidenceIds: List<String>): List<String> =
        skillsOf(evidenceIds).filter(profile::isSkillUserStated)

    fun factRefsOf(evidenceIds: List<String>): List<RequirementFactRef> {
        val linesByEntry = linkedMapOf<ProfileEntry, MutableList<String>>()
        evidenceIds.filterNot { it.startsWith(SKILL_ID_PREFIX) }.forEach { id ->
            val entry = entryByBulletId[id] ?: entryById[id] ?: return@forEach
            val lines = linesByEntry.getOrPut(entry) { mutableListOf() }
            bulletTextById[id]?.let(lines::add)
        }
        return linesByEntry.map { (entry, lines) -> entry.toRef(lines) }
    }

    private fun ProfileEntry.toRef(lines: List<String>) = RequirementFactRef(
        factId = id,
        displayId = FactDisplayIds.of(this, profile.entries),
        title = title,
        organization = organization,
        startDate = startDate,
        endDate = endDate,
        lines = lines,
        source = source,
        isConfirmed = isConfirmed,
    )

    private companion object {
        const val SKILL_ID_PREFIX = "skill:"
    }
}
