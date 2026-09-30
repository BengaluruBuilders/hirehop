package com.hirehop.feature.applications.impl

import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EditType
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementMatch
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.model.TailoredResume
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

private fun previewRequirement(id: String, text: String, priority: RequirementPriority) =
    JobRequirement(
        id = id,
        text = text,
        type = RequirementType.SKILL,
        priority = priority,
        keywords = listOf(text.lowercase()),
    )

private fun previewBullet(id: String, decision: BulletDecision) = TailoredBullet(
    id = id,
    entryId = "entry-1",
    originalText = "Built the checkout screen",
    proposedText = "Built the checkout screen with Jetpack Compose",
    sourceIds = listOf("bullet-1"),
    editTypes = listOf(EditType.REWORD),
    keywordsUsed = listOf("jetpack compose"),
    violations = emptyList(),
    decision = decision,
)

private fun previewGapAnalysis() = GapAnalysis(
    matches = listOf(
        RequirementMatch(
            requirement = previewRequirement("r1", "Kotlin", RequirementPriority.MUST_HAVE),
            status = MatchStatus.MET,
            evidenceIds = listOf("bullet-1"),
        ),
        RequirementMatch(
            requirement = previewRequirement("r2", "Jetpack Compose", RequirementPriority.MUST_HAVE),
            status = MatchStatus.PARTIAL,
            evidenceIds = listOf("bullet-2"),
        ),
        RequirementMatch(
            requirement = previewRequirement("r3", "GraphQL", RequirementPriority.MUST_HAVE),
            status = MatchStatus.GAP,
            evidenceIds = emptyList(),
        ),
        RequirementMatch(
            requirement = previewRequirement("r4", "Firebase", RequirementPriority.NICE_TO_HAVE),
            status = MatchStatus.GAP,
            evidenceIds = emptyList(),
        ),
    ),
    keywordCoverage = KeywordCoverage(covered = 6, total = 10),
)

internal fun previewApplication(
    id: String = "app-1",
    title: String = "Android Engineer",
    company: String = "Acme Labs",
    status: ApplicationStatus = ApplicationStatus.APPLIED,
): JobApplication {
    val now = Clock.System.now()
    return JobApplication(
        id = id,
        job = JobDescription(title = title, company = company, rawText = "", requirements = emptyList()),
        status = status,
        notes = "Recruiter call on Friday.",
        gapAnalysis = previewGapAnalysis(),
        tailoredResume = TailoredResume(
            bullets = listOf(
                previewBullet("b1", BulletDecision.ACCEPTED),
                previewBullet("b2", BulletDecision.PENDING),
                previewBullet("b3", BulletDecision.REJECTED),
            ),
        ),
        createdAt = now - 3.days,
        updatedAt = now - 2.hours,
    )
}

internal fun previewApplications(): List<JobApplication> = listOf(
    previewApplication(),
    previewApplication(
        id = "app-2",
        title = "Senior Mobile Developer",
        company = "Northwind",
        status = ApplicationStatus.INTERVIEW,
    ),
    previewApplication(
        id = "app-3",
        title = "Kotlin Backend Engineer",
        company = "Globex",
        status = ApplicationStatus.SAVED,
    ),
)
