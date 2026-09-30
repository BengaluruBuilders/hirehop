package com.hirehop.core.data.model

import com.hirehop.core.database.json.GapAnalysisDto
import com.hirehop.core.database.json.JobRequirementDto
import com.hirehop.core.database.json.KeywordCoverageDto
import com.hirehop.core.database.json.RequirementMatchDto
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.RequirementMatch

fun JobRequirement.asDto() = JobRequirementDto(
    id = id,
    text = text,
    type = type,
    priority = priority,
    keywords = keywords,
)

fun JobRequirementDto.asExternalModel() = JobRequirement(
    id = id,
    text = text,
    type = type,
    priority = priority,
    keywords = keywords,
)

fun RequirementMatch.asDto() = RequirementMatchDto(
    requirement = requirement.asDto(),
    status = status,
    evidenceIds = evidenceIds,
)

fun RequirementMatchDto.asExternalModel() = RequirementMatch(
    requirement = requirement.asExternalModel(),
    status = status,
    evidenceIds = evidenceIds,
)

fun KeywordCoverage.asDto() = KeywordCoverageDto(covered = covered, total = total)

fun KeywordCoverageDto.asExternalModel() = KeywordCoverage(covered = covered, total = total)

fun GapAnalysis.asDto() = GapAnalysisDto(
    matches = matches.map(RequirementMatch::asDto),
    keywordCoverage = keywordCoverage.asDto(),
)

fun GapAnalysisDto.asExternalModel() = GapAnalysis(
    matches = matches.map(RequirementMatchDto::asExternalModel),
    keywordCoverage = keywordCoverage.asExternalModel(),
)
