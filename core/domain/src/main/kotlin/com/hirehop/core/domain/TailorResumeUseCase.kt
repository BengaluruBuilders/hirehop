package com.hirehop.core.domain

import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.model.TailoredResume
import javax.inject.Inject

class TailorResumeUseCase @Inject constructor(
    private val tailor: ResumeTailor,
    private val guard: FabricationGuard,
) {
    operator fun invoke(
        profile: CandidateProfile,
        job: JobDescription,
        gap: GapAnalysis,
    ): TailoredResume {
        val confirmedSources = profile.entries
            .filter { it.isConfirmed }
            .flatMap { it.bullets }
            .associateBy { it.id }
        val proposed = tailor.tailor(profile, job, gap)
        return TailoredResume(proposed.bullets.map { verified(it, confirmedSources, profile) })
    }

    private fun verified(
        bullet: TailoredBullet,
        confirmedSources: Map<String, EvidenceBullet>,
        profile: CandidateProfile,
    ): TailoredBullet {
        val sources = bullet.sourceIds.mapNotNull(confirmedSources::get)
        val violations = guard.check(bullet.proposedText, sources, profile)
        val sourceText = sources.firstOrNull()?.text ?: bullet.originalText
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
