package com.hirehop.feature.profile.impl.evidencepath

import com.hirehop.core.model.EntryCategory

enum class EvidenceCategory(val key: String) {
    PROJECTS("projects"),
    INTERNSHIPS("internships"),
    COURSEWORK("coursework"),
    COMPETITIONS("competitions"),
    POSITIONS("positions"),
    ;

    val entryCategory: EntryCategory
        get() = when (this) {
            EvidenceCategory.PROJECTS -> EntryCategory.PROJECT
            EvidenceCategory.INTERNSHIPS -> EntryCategory.EXPERIENCE
            EvidenceCategory.COURSEWORK -> EntryCategory.EDUCATION
            EvidenceCategory.COMPETITIONS -> EntryCategory.ACHIEVEMENT
            EvidenceCategory.POSITIONS -> EntryCategory.ACHIEVEMENT
        }

    val asksOrganization: Boolean
        get() = this == EvidenceCategory.INTERNSHIPS || this == EvidenceCategory.POSITIONS
}

val EVIDENCE_CATEGORIES: List<EvidenceCategory> = listOf(
    EvidenceCategory.PROJECTS,
    EvidenceCategory.INTERNSHIPS,
    EvidenceCategory.COURSEWORK,
    EvidenceCategory.COMPETITIONS,
    EvidenceCategory.POSITIONS,
)

enum class EvidencePrompt { TITLE, DETAIL, ORGANIZATION }

fun EvidenceCategory.prompts(): List<EvidencePrompt> = if (asksOrganization) {
    listOf(EvidencePrompt.TITLE, EvidencePrompt.DETAIL, EvidencePrompt.ORGANIZATION)
} else {
    listOf(EvidencePrompt.TITLE, EvidencePrompt.DETAIL)
}

fun evidenceCategoryOf(key: String): EvidenceCategory =
    EVIDENCE_CATEGORIES.firstOrNull { it.key.equals(key.trim(), ignoreCase = true) }
        ?: EvidenceCategory.PROJECTS
