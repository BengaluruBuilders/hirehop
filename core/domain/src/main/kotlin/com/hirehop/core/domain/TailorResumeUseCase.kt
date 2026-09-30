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
        val sourcesById = profile.entries.flatMap { it.bullets }.associateBy { it.id }
        val proposed = tailor.tailor(profile, job, gap)
        return TailoredResume(proposed.bullets.map { verified(it, sourcesById, profile) })
    }

    private fun verified(
        bullet: TailoredBullet,
        sourcesById: Map<String, EvidenceBullet>,
        profile: CandidateProfile,
    ): TailoredBullet {
        val sources = bullet.sourceIds.mapNotNull(sourcesById::get)
        val violations = guard.check(bullet.proposedText, sources, profile)
        if (violations.isEmpty()) return bullet.copy(violations = emptyList())
        return bullet.copy(
            proposedText = bullet.originalText,
            editTypes = emptyList(),
            violations = violations,
        )
    }
}
