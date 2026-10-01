package com.hirehop.feature.tailor.impl

import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.model.TailoredResume
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import com.hirehop.feature.tailor.impl.document.isFreshFor
import com.hirehop.feature.tailor.impl.export.ExportFileName

internal fun buildTailorUiState(
    application: JobApplication?,
    profile: CandidateProfile?,
    assembler: ResumeDocumentAssembler,
): TailorUiState {
    val resume = application?.tailoredResume
    if (application == null || profile == null || resume == null) return TailorUiState.NotFound
    val entries = buildEntries(profile, resume)
    val reviewable = entries.flatMap { it.bullets }.filter { it.kind == BulletReviewKind.REVIEWABLE }
    val document = assembler.assemble(profile, resume)
    return TailorUiState.Success(
        entries = entries,
        reviewedCount = reviewable.count { it.bullet.decision != BulletDecision.PENDING },
        totalCount = reviewable.size,
        canExport = !document.isEmpty,
        document = document,
        exportFileName = ExportFileName.build(document.name, application.job.company, application.job.title),
    )
}

private fun buildEntries(profile: CandidateProfile, resume: TailoredResume): List<TailorEntryUi> {
    val confirmed = profile.entries.filter { it.isConfirmed }
    val sourceById = mutableMapOf<String, TailoredBulletSource>()
    confirmed.forEach { entry ->
        entry.bullets.forEach { bullet ->
            sourceById[bullet.id] = TailoredBulletSource(
                id = bullet.id,
                text = bullet.text,
                source = entry.source,
            )
        }
    }
    val bulletsByEntry = resume.bullets.groupBy { it.entryId }
    return confirmed.mapNotNull { entry ->
        bulletsByEntry[entry.id]
            ?.takeIf { it.isNotEmpty() }
            ?.let { entry.toUi(it, sourceById) }
    }
}

private fun ProfileEntry.toUi(
    tailored: List<TailoredBullet>,
    sourceById: Map<String, TailoredBulletSource>,
): TailorEntryUi = TailorEntryUi(
    entryId = id,
    category = category,
    title = title,
    organization = organization,
    bullets = tailored.map { bullet ->
        TailorBulletUi(
            bullet = bullet,
            sourceTexts = bullet.sourceIds.mapNotNull { sourceById[it]?.text },
            sources = bullet.sourceIds.mapNotNull { sourceById[it] },
            isStale = !bullet.isFreshFor(this),
        )
    },
)
