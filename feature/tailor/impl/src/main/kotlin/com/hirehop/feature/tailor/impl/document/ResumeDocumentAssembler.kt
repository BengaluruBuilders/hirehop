package com.hirehop.feature.tailor.impl.document

import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.TailoredResume
import javax.inject.Inject

internal class ResumeDocumentAssembler @Inject constructor() {

    fun assemble(profile: CandidateProfile, resume: TailoredResume): ResumeDocument {
        val confirmedEntries = profile.entries.filter { it.isConfirmed }
        return ResumeDocument(
            name = profile.fullName.trim(),
            contactLine = listOf(profile.email, profile.phone).cleaned().joinToString(CONTACT_SEPARATOR),
            headline = profile.headline.trim(),
            skills = profile.skills.cleaned().distinctBy { it.lowercase() },
            sections = EntryCategory.entries.mapNotNull { category ->
                confirmedEntries
                    .filter { it.category == category }
                    .map { it.toResumeEntry(resume) }
                    .takeIf { it.isNotEmpty() }
                    ?.let { ResumeSection(category, category.resumeHeading, it) }
            },
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
