package com.hirehop.feature.tailor.impl

import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.data.repository.TailoringReviewStateRepository
import com.hirehop.core.domain.AiException
import com.hirehop.core.domain.TailorResumeUseCase
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.TailoredResume
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.time.Clock

internal class RegenerateSectionUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val tailorResume: TailorResumeUseCase,
    private val reviewState: TailoringReviewStateRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(applicationId: String, category: EntryCategory): Boolean {
        if (reviewState.observe(applicationId).first().regenerationsUsed >= MAX_REGENERATIONS) return false
        val application = applicationRepository.observeApplication(applicationId).first() ?: return false
        val profile = profileRepository.observeProfile().first() ?: return false
        val gap = application.gapAnalysis ?: return false
        val sectionEntryIds = profile.entries
            .filter { it.isConfirmed && it.category == category }
            .map { it.id }
            .toSet()
        val existing = application.tailoredResume?.bullets.orEmpty()
        val fresh = try {
            tailorResume(profile, application.job, gap).bullets
        } catch (_: AiException) {
            return false
        }
            .filter { it.entryId in sectionEntryIds }
            .map { it.copy(decision = BulletDecision.PENDING) }
        val kept = existing.filter { it.entryId !in sectionEntryIds }
        applicationRepository.upsertApplication(
            application.copy(
                tailoredResume = TailoredResume(
                    bullets = kept + fresh,
                    entryIds = application.tailoredResume?.entryIds?.let { (it + sectionEntryIds).distinct() },
                ),
                updatedAt = clock.now(),
            ),
        )
        reviewState.clearEdited(applicationId, existing.filter { it.entryId in sectionEntryIds }.map { it.id })
        reviewState.recordRegeneration(applicationId, category.name)
        return true
    }
}
