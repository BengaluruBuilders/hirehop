package com.hirehop.feature.tailor.impl.document

import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.model.TailoredResume
import javax.inject.Inject

internal class ResumeDocumentAssembler @Inject constructor() {

    fun assemble(profile: CandidateProfile, resume: TailoredResume): ResumeDocument {
        val bulletsByEntry = resume.bullets.groupBy { it.entryId }
        val confirmedEntries = profile.entries.filter { it.isConfirmed }
        return ResumeDocument(
            name = profile.fullName.trim(),
            contactLine = listOf(profile.email, profile.phone).cleaned().joinToString(CONTACT_SEPARATOR),
            headline = profile.headline.trim(),
            skills = profile.skills.cleaned().distinctBy { it.lowercase() },
            sections = EntryCategory.entries.mapNotNull { category ->
                confirmedEntries
                    .filter { it.category == category }
                    .map { it.toResumeEntry(bulletsByEntry[it.id].orEmpty()) }
                    .takeIf { it.isNotEmpty() }
                    ?.let { ResumeSection(category, category.resumeHeading, it) }
            },
        )
    }

    private fun ProfileEntry.toResumeEntry(tailored: List<TailoredBullet>): ResumeEntry {
        val texts = if (tailored.isEmpty()) bullets.map { it.text } else tailored.map { it.finalText() }
        return ResumeEntry(
            title = title.trim(),
            organization = organization.trim(),
            dateRange = dateRange(startDate, endDate),
            bullets = texts.cleaned(),
        )
    }

    private fun dateRange(start: String, end: String): String =
        listOf(start, end).cleaned().joinToString(DATE_SEPARATOR)

    private fun List<String>.cleaned(): List<String> = map { it.trim() }.filter { it.isNotEmpty() }

    private companion object {
        const val CONTACT_SEPARATOR = " | "
        const val DATE_SEPARATOR = " - "
    }
}

internal fun TailoredBullet.finalText(): String = when {
    violations.isNotEmpty() -> originalText
    decision == BulletDecision.ACCEPTED -> proposedText
    else -> originalText
}
