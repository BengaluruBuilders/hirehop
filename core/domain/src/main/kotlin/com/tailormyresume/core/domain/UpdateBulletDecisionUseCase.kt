package com.tailormyresume.core.domain

import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.model.BulletDecision
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.core.model.TailoredResume
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.time.Clock

class UpdateBulletDecisionUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(applicationId: String, bulletId: String, decision: BulletDecision) {
        val application = applicationRepository.observeApplication(applicationId).first() ?: return
        val resume = application.tailoredResume ?: return
        applicationRepository.upsertApplication(application.withDecision(resume, bulletId, decision))
    }

    private fun JobApplication.withDecision(
        resume: TailoredResume,
        bulletId: String,
        decision: BulletDecision,
    ): JobApplication {
        val bullets = resume.bullets.map { if (it.id == bulletId) it.copy(decision = decision) else it }
        return copy(tailoredResume = resume.copy(bullets = bullets), updatedAt = clock.now())
    }
}
