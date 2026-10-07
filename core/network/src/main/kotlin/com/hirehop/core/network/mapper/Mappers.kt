package com.hirehop.core.network.mapper

import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.ContentReport
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.RequirementMatch
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.network.dto.BulletDto
import com.hirehop.core.network.dto.ContentReportRequest
import com.hirehop.core.network.dto.FactEntryDto
import com.hirehop.core.network.dto.JobDto
import com.hirehop.core.network.dto.MatchDto
import com.hirehop.core.network.dto.ProfileFactsDto
import com.hirehop.core.network.dto.RequirementDto
import com.hirehop.core.network.dto.TailoredBulletDto

fun CandidateProfile.toFactsDto(): ProfileFactsDto = ProfileFactsDto(
    skills = skills,
    entries = entries.filter { it.isConfirmed }.map { entry ->
        FactEntryDto(
            id = entry.id,
            category = entry.category,
            title = entry.title,
            organization = entry.organization,
            startDate = entry.startDate,
            endDate = entry.endDate,
            source = entry.source,
            bullets = entry.bullets.map { BulletDto(it.id, it.text) },
        )
    },
)

fun JobDescription.toDto(): JobDto = JobDto(
    title = title,
    company = company,
    requirements = requirements.map { RequirementDto(it.id, it.text, it.type, it.priority, it.keywords) },
)

fun JobDto.toJobDescription(rawText: String): JobDescription = JobDescription(
    title = title,
    company = company,
    rawText = rawText,
    requirements = requirements.map { JobRequirement(it.id, it.text, it.type, it.priority, it.keywords) },
)

fun RequirementMatch.toDto(): MatchDto = MatchDto(requirement.id, status, evidenceIds)

fun List<MatchDto>.toRequirementMatches(job: JobDescription): List<RequirementMatch> {
    val requirementsById = job.requirements.associateBy { it.id }
    return mapNotNull { match ->
        requirementsById[match.requirementId]?.let { RequirementMatch(it, match.status, match.evidenceIds) }
    }
}

fun TailoredBulletDto.toTailoredBullet(originalText: String): TailoredBullet = TailoredBullet(
    id = id,
    entryId = entryId,
    originalText = originalText,
    proposedText = proposedText,
    sourceIds = sourceIds,
    editTypes = editTypes,
    keywordsUsed = keywordsUsed,
    violations = emptyList(),
    decision = BulletDecision.PENDING,
)

fun ContentReport.toRequest(generationId: String?, itemText: String): ContentReportRequest =
    ContentReportRequest(applicationId, itemKind, itemId, generationId, itemText)
