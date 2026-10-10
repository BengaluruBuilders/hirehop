package com.tailormyresume.core.domain

import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.QuickAnswer
import com.tailormyresume.core.model.TailoredBullet
import com.tailormyresume.core.model.TailoredResume
import com.tailormyresume.core.model.confirmedWithinLimits
import javax.inject.Inject

class TailorResumeUseCase @Inject constructor(
    private val tailor: ResumeTailor,
    private val guard: FabricationGuard,
) {
    suspend operator fun invoke(
        profile: CandidateProfile,
        job: JobDescription,
        gap: GapAnalysis,
        applicationId: String,
        section: EntryCategory? = null,
        quickAnswer: QuickAnswer? = null,
    ): TailoredResume {
        val confirmedSources = profile.confirmedWithinLimits().entries
            .flatMap { it.bullets }
            .associateBy { it.id }
        val proposed = tailor.tailor(profile, job, gap, applicationId, section)
        return TailoredResume(
            bullets = proposed.bullets.mapNotNull { verified(it, confirmedSources, profile) },
            entryIds = profile.entries.filter { it.isConfirmed }.map { it.id },
        )
    }

    private fun verified(
        bullet: TailoredBullet,
        confirmedSources: Map<String, EvidenceBullet>,
        profile: CandidateProfile,
    ): TailoredBullet? {
        val sources = bullet.sourceIds.mapNotNull(confirmedSources::get)
        val sourceText = sources.firstOrNull()?.text ?: return null
        val violations = guard.check(bullet.proposedText, sources, profile)
        if (violations.isEmpty()) return bullet.copy(originalText = sourceText, violations = emptyList())
        return bullet.copy(
            originalText = sourceText,
            proposedText = sourceText,
            editTypes = emptyList(),
            keywordsUsed = emptyList(),
            violations = violations,
        )
    }
}
