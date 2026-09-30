package com.hirehop.core.testing.data

import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementMatch
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import kotlin.time.Instant

val sampleKotlinRequirement = JobRequirement(
    id = "requirement-kotlin",
    text = "Experience with Kotlin",
    type = RequirementType.SKILL,
    priority = RequirementPriority.MUST_HAVE,
    keywords = listOf("kotlin"),
)

val sampleKubernetesRequirement = JobRequirement(
    id = "requirement-kubernetes",
    text = "Familiarity with Kubernetes",
    type = RequirementType.TOOL,
    priority = RequirementPriority.NICE_TO_HAVE,
    keywords = listOf("kubernetes"),
)

val sampleJobDescription = JobDescription(
    title = "Android Developer Intern",
    company = "Example Corp",
    rawText = "Android Developer Intern. Experience with Kotlin. Familiarity with Kubernetes.",
    requirements = listOf(sampleKotlinRequirement, sampleKubernetesRequirement),
)

val sampleGapAnalysis = GapAnalysis(
    matches = listOf(
        RequirementMatch(
            requirement = sampleKotlinRequirement,
            status = MatchStatus.MET,
            evidenceIds = listOf("bullet-project-1"),
        ),
        RequirementMatch(
            requirement = sampleKubernetesRequirement,
            status = MatchStatus.GAP,
            evidenceIds = emptyList(),
        ),
    ),
    keywordCoverage = KeywordCoverage(covered = 1, total = 2),
)

val sampleApplication = JobApplication(
    id = "application-1",
    job = sampleJobDescription,
    status = ApplicationStatus.SAVED,
    notes = "",
    gapAnalysis = sampleGapAnalysis,
    tailoredResume = null,
    createdAt = Instant.fromEpochSeconds(1_700_000_000),
    updatedAt = Instant.fromEpochSeconds(1_700_000_000),
)
