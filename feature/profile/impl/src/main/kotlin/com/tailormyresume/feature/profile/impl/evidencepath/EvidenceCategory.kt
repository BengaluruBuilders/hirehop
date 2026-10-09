package com.tailormyresume.feature.profile.impl.evidencepath

import com.tailormyresume.core.model.CareerStage
import com.tailormyresume.core.model.EntryCategory

enum class EvidenceCategory(
    val key: String,
    val entryCategory: EntryCategory,
    val questionCount: Int,
) {
    WORK("work", EntryCategory.EXPERIENCE, 4),
    PROJECTS("projects", EntryCategory.PROJECT, 4),
    INTERNSHIPS("internships", EntryCategory.EXPERIENCE, 4),
    COURSEWORK("coursework", EntryCategory.EDUCATION, 4),
    COMPETITIONS("competitions", EntryCategory.ACHIEVEMENT, 4),
    POSITIONS("positions", EntryCategory.ACHIEVEMENT, 4),
}

val EVIDENCE_CATEGORIES: List<EvidenceCategory> = EvidenceCategory.entries

fun evidenceCategoriesFor(stage: CareerStage?): List<EvidenceCategory> = when (stage) {
    CareerStage.JUST_STARTING_OUT -> listOf(
        EvidenceCategory.PROJECTS,
        EvidenceCategory.INTERNSHIPS,
        EvidenceCategory.COURSEWORK,
        EvidenceCategory.COMPETITIONS,
        EvidenceCategory.POSITIONS,
        EvidenceCategory.WORK,
    )

    CareerStage.ONE_TO_TWO_YEARS_IN, null -> EVIDENCE_CATEGORIES
}

fun evidenceCategoryOrNull(key: String): EvidenceCategory? =
    EVIDENCE_CATEGORIES.firstOrNull { it.key.equals(key.trim(), ignoreCase = true) }

internal data class AnswerParts(
    val title: String,
    val detail: String,
)

internal fun splitAnswer(answer: String): AnswerParts {
    val text = answer.trim()
    val match = SENTENCE_BOUNDARY.find(text)
    val head = (if (match == null) text else text.substring(0, match.range.first)).trim().trimEnd('.')
    val tail = if (match == null) "" else text.substring(match.range.last + 1).trim()
    if (head.length <= TITLE_LIMIT) return AnswerParts(title = head, detail = tail)
    val cut = head.lastIndexOf(' ', TITLE_LIMIT).takeIf { it > 0 } ?: TITLE_LIMIT
    return AnswerParts(
        title = head.substring(0, cut).trim(),
        detail = listOf(head.substring(cut).trim(), tail).filter { it.isNotEmpty() }.joinToString(" "),
    )
}

private val SENTENCE_BOUNDARY = Regex("""[.!?]\s+|\s*\n\s*""")
private const val TITLE_LIMIT = 80
