package com.tailormyresume.feature.tailor.impl

import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.data.repository.TailoringReviewStateRepository
import com.tailormyresume.core.domain.AiException
import com.tailormyresume.core.domain.AiFailure
import com.tailormyresume.core.domain.TailorResumeUseCase
import com.tailormyresume.core.model.BulletDecision
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.TailoredResume
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.time.Clock

internal sealed interface RegenerateResult {
    data object Done : RegenerateResult

    data object Skipped : RegenerateResult

    data object NoCredit : RegenerateResult

    data object Failed : RegenerateResult

    data class Blocked(val notice: AiNotice) : RegenerateResult
}

internal class RegenerateSectionUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val tailorResume: TailorResumeUseCase,
    private val reviewState: TailoringReviewStateRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(applicationId: String, category: EntryCategory): RegenerateResult {
        if (reviewState.observe(applicationId).first().regenerationsUsed >= MAX_REGENERATIONS) return RegenerateResult.Skipped
        val application = applicationRepository.observeApplication(applicationId).first() ?: return RegenerateResult.Skipped
        val profile = profileRepository.observeProfile().first() ?: return RegenerateResult.Skipped
        val gap = application.gapAnalysis ?: return RegenerateResult.Skipped
        val sectionEntryIds = profile.entries
            .filter { it.isConfirmed && it.category == category }
            .map { it.id }
            .toSet()
        val existing = application.tailoredResume?.bullets.orEmpty()
        val fresh = try {
            tailorResume(profile, application.job, gap, applicationId, category).bullets
        } catch (e: AiException) {
            val notice = e.toAiNotice()
            return when {
                e.failure == AiFailure.NoCredit -> RegenerateResult.NoCredit
                notice == AiNotice.Generic -> RegenerateResult.Failed
                else -> RegenerateResult.Blocked(notice)
            }
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
        return RegenerateResult.Done
    }
}
