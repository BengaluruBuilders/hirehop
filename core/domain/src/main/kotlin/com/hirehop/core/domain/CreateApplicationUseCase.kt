package com.hirehop.core.domain

import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.KeptJobDescription
import javax.inject.Inject
import kotlin.time.Clock

class CreateApplicationUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val tailorResume: TailorResumeUseCase,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) {
    suspend operator fun invoke(
        profile: CandidateProfile,
        analysis: JobAnalysisResult,
        kept: KeptJobDescription? = null,
    ): String {
        val job = kept?.let { analysis.job.withKeptLabel(it) } ?: analysis.job
        val tailored = tailorResume(profile, job, analysis.gap)
        val now = clock.now()
        val application = JobApplication(
            id = idGenerator.newId(),
            job = job,
            status = ApplicationStatus.SAVED,
            notes = "",
            gapAnalysis = analysis.gap,
            tailoredResume = tailored,
            createdAt = now,
            updatedAt = now,
        )
        applicationRepository.upsertApplication(application)
        return application.id
    }

    private fun JobDescription.withKeptLabel(kept: KeptJobDescription) = copy(
        title = kept.role.trim().ifEmpty { title },
        company = kept.company.trim().ifEmpty { company },
    )
}
