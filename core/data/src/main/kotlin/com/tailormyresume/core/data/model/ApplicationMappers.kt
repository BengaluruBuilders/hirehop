package com.tailormyresume.core.data.model

import com.tailormyresume.core.database.json.JobRequirementDto
import com.tailormyresume.core.database.model.JobApplicationEntity
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.JobRequirement

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
