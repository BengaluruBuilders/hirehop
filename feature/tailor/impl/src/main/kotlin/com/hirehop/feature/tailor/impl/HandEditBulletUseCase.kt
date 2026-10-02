package com.hirehop.feature.tailor.impl

import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.TailoringReviewStateRepository
import com.hirehop.core.model.BulletDecision
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.time.Clock

internal class HandEditBulletUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val reviewState: TailoringReviewStateRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(applicationId: String, bulletId: String, text: String): Boolean {
        val edited = text.trim()
        if (edited.isEmpty()) return false
        val application = applicationRepository.observeApplication(applicationId).first() ?: return false
        val resume = application.tailoredResume ?: return false
        if (resume.bullets.none { it.id == bulletId }) return false
        val bullets = resume.bullets.map {
            if (it.id == bulletId) {
                it.copy(
                    proposedText = edited,
                    decision = BulletDecision.ACCEPTED,
                    violations = emptyList(),
                    editTypes = emptyList(),
                    keywordsUsed = emptyList(),
                )
            } else {
                it
            }
        }
        applicationRepository.upsertApplication(
            application.copy(tailoredResume = resume.copy(bullets = bullets), updatedAt = clock.now()),
        )
        reviewState.markEdited(applicationId, bulletId)
        return true
    }
}
