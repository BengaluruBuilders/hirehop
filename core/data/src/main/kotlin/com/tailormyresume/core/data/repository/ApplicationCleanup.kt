package com.tailormyresume.core.data.repository

import javax.inject.Inject

internal fun interface ApplicationCleanup {
    suspend fun clearFor(applicationId: String)
}

internal class StoredApplicationCleanup @Inject constructor(
    private val prepPlan: PrepPlanRepository,
    private val reports: ContentReportRepository,
    private val reviewState: TailoringReviewStateRepository,
    private val coverLetters: CoverLetterRepository,
) : ApplicationCleanup {
    override suspend fun clearFor(applicationId: String) {
        prepPlan.clearFor(applicationId)
        reports.clearFor(applicationId)
        reviewState.clearFor(applicationId)
        coverLetters.clearFor(applicationId)
    }
}
