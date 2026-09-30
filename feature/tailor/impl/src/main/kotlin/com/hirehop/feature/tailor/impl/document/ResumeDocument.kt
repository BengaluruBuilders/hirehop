package com.hirehop.feature.tailor.impl.document

import com.hirehop.core.model.EntryCategory

internal data class ResumeDocument(
    val name: String,
    val contactLine: String,
    val headline: String,
    val skills: List<String>,
    val sections: List<ResumeSection>,
) {
    val isEmpty: Boolean get() = sections.isEmpty()
}

internal data class ResumeSection(
    val category: EntryCategory,
    val heading: String,
    val entries: List<ResumeEntry>,
)

internal data class ResumeEntry(
    val title: String,
    val organization: String,
    val dateRange: String,
    val bullets: List<String>,
)

internal const val SKILLS_HEADING = "Skills"

internal val EntryCategory.resumeHeading: String
    get() = when (this) {
        EntryCategory.EDUCATION -> "Education"
        EntryCategory.EXPERIENCE -> "Experience"
        EntryCategory.PROJECT -> "Projects"
        EntryCategory.CERTIFICATION -> "Certifications"
        EntryCategory.ACHIEVEMENT -> "Achievements"
    }
