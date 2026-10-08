package com.tailormyresume.feature.tailor.impl.document

import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.TailoredResume
import javax.inject.Inject

internal class ResumeDocumentAssembler @Inject constructor(
    private val headings: ResumeHeadings,
) {

    fun assemble(profile: CandidateProfile, resume: TailoredResume): ResumeDocument {
        val reviewed = reviewedEntries(profile, resume)
        val recordedIds = resume.entryIds?.toSet()
        return ResumeDocument(
            name = profile.fullName.trim(),
            contactLine = listOf(profile.email, profile.phone).cleaned().joinToString(CONTACT_SEPARATOR),
            headline = profile.headline.trim(),
            skills = profile.skills.cleaned().distinctBy { it.lowercase() },
            sections = EntryCategory.entries.mapNotNull { category ->
                reviewed
                    .filter { it.category == category }
                    .filter { entry -> entry.bullets.isNotEmpty() || recordedIds == null || entry.id in recordedIds }
                    .map { it.toResumeEntry(resume) }
                    .takeIf { it.isNotEmpty() }
                    ?.let { ResumeSection(category, headings.forCategory(category), it) }
            },
            skillsHeading = headings.skills,
        )
    }

    private fun ProfileEntry.toResumeEntry(resume: TailoredResume): ResumeEntry = ResumeEntry(
        title = title.trim(),
        organization = organization.trim(),
        dateRange = dateRange(startDate, endDate),
        bullets = BulletReconciler.reconcile(this, resume).cleaned(),
    )

    private fun dateRange(start: String, end: String): String =
        listOf(start, end).cleaned().joinToString(DATE_SEPARATOR)

    private fun List<String>.cleaned(): List<String> = map { it.trim() }.filter { it.isNotEmpty() }

    private companion object {
        const val CONTACT_SEPARATOR = " | "
        const val DATE_SEPARATOR = " - "
    }
}
