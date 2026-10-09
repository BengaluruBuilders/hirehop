package com.tailormyresume.core.domain

import com.tailormyresume.core.model.RequirementMatch

fun upgradedMatch(
    server: RequirementMatch,
    current: RequirementMatch?,
    baseline: RequirementMatch?,
    targeted: Boolean,
): RequirementMatch? {
    if (current == null || current.status.ordinal >= server.status.ordinal) return null
    val baselineIds = baseline?.evidenceIds.orEmpty()
    val improvesStatus = current.status.ordinal < (baseline?.status?.ordinal ?: Int.MAX_VALUE)
    val addsEvidence = current.evidenceIds.any { it !in baselineIds }
    if (!targeted && !improvesStatus && !addsEvidence) return null
    return RequirementMatch(
        requirement = server.requirement,
        status = current.status,
        evidenceIds = (server.evidenceIds + current.evidenceIds).distinct(),
    )
}
