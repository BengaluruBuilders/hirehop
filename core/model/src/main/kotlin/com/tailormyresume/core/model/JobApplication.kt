package com.tailormyresume.core.model

import kotlin.time.Instant

enum class ApplicationStatus { SAVED, APPLIED, INTERVIEW, OFFER, REJECTED, NO_RESPONSE }

data class JobApplication(
    val id: String,
    val job: JobDescription,
    val status: ApplicationStatus,
    val notes: String,
    val gapAnalysis: GapAnalysis?,
    val tailoredResume: TailoredResume?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
