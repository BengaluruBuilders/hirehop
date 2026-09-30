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
import kotlin.time.Instant

internal fun testMatch(id: String, status: MatchStatus, priority: RequirementPriority) =
    RequirementMatch(
        requirement = JobRequirement(
            id = id,
            text = "Requirement $id",
            type = RequirementType.SKILL,
            priority = priority,
            keywords = listOf(id),
        ),
        status = status,
        evidenceIds = emptyList(),
    )

internal fun testBullet(
    id: String,
    decision: BulletDecision,
    editTypes: List<EditType> = listOf(EditType.REWORD),
) = TailoredBullet(
    id = id,
    entryId = "entry",
    originalText = "Original $id",
    proposedText = "Proposed $id",
    sourceIds = listOf("source"),
    editTypes = editTypes,
    keywordsUsed = emptyList(),
    violations = emptyList(),
    decision = decision,
)

internal fun testApplication(
    id: String,
    updatedAtEpochSeconds: Long = 0,
    status: ApplicationStatus = ApplicationStatus.SAVED,
    notes: String = "",
    gapAnalysis: GapAnalysis? = null,
    tailoredResume: TailoredResume? = null,
) = JobApplication(
    id = id,
    job = JobDescription(
        title = "Role $id",
        company = "Company $id",
        rawText = "",
        requirements = emptyList(),
    ),
    status = status,
    notes = notes,
    gapAnalysis = gapAnalysis,
    tailoredResume = tailoredResume,
    createdAt = Instant.fromEpochSeconds(0),
    updatedAt = Instant.fromEpochSeconds(updatedAtEpochSeconds),
)

internal fun testGapAnalysis() = GapAnalysis(
    matches = listOf(
        testMatch("a", MatchStatus.MET, RequirementPriority.MUST_HAVE),
        testMatch("b", MatchStatus.MET, RequirementPriority.NICE_TO_HAVE),
        testMatch("c", MatchStatus.PARTIAL, RequirementPriority.MUST_HAVE),
        testMatch("d", MatchStatus.GAP, RequirementPriority.MUST_HAVE),
        testMatch("e", MatchStatus.GAP, RequirementPriority.NICE_TO_HAVE),
    ),
    keywordCoverage = KeywordCoverage(covered = 3, total = 5),
)

internal fun testTailoredResume() = TailoredResume(
    bullets = listOf(
        testBullet("1", BulletDecision.ACCEPTED),
        testBullet("2", BulletDecision.REJECTED),
        testBullet("3", BulletDecision.PENDING),
        testBullet("4", BulletDecision.PENDING, editTypes = emptyList()),
    ),
)
