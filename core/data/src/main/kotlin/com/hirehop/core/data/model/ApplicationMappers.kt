package com.hirehop.core.data.model

import com.hirehop.core.database.json.JobRequirementDto
import com.hirehop.core.database.model.JobApplicationEntity
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.JobRequirement

fun JobApplication.asEntity() = JobApplicationEntity(
    id = id,
    jobTitle = job.title,
    company = job.company,
    rawText = job.rawText,
    requirements = job.requirements.map(JobRequirement::asDto),
    status = status,
    notes = notes,
    gapAnalysis = gapAnalysis?.asDto(),
    tailoredResume = tailoredResume?.asDto(),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun JobApplicationEntity.asExternalModel() = JobApplication(
    id = id,
    job = JobDescription(
        title = jobTitle,
        company = company,
        rawText = rawText,
        requirements = requirements.map(JobRequirementDto::asExternalModel),
    ),
    status = status,
    notes = notes,
    gapAnalysis = gapAnalysis?.asExternalModel(),
    tailoredResume = tailoredResume?.asExternalModel(),
    createdAt = createdAt,
    updatedAt = updatedAt,
)
