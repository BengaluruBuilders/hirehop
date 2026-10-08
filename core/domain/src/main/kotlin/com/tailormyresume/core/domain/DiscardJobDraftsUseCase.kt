package com.tailormyresume.core.domain

import com.tailormyresume.core.data.repository.ContentReportRepository
import com.tailormyresume.core.data.repository.PrepPlanRepository
import com.tailormyresume.core.model.KeptJobDescription
import javax.inject.Inject

class DiscardJobDraftsUseCase @Inject constructor(
    private val prepPlanRepository: PrepPlanRepository,
    private val contentReportRepository: ContentReportRepository,
) {
    suspend operator fun invoke(job: KeptJobDescription) {
        prepPlanRepository.clearFor(job.draftKey)
        contentReportRepository.clearFor(job.draftKey)
    }
}
