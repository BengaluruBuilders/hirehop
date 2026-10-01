package com.hirehop.feature.profile.impl.guidedform

import com.hirehop.core.domain.fact.FactDraft
import com.hirehop.core.model.EntryCategory

enum class GuidedStep {
    CONTACT,
    EDUCATION,
    SKILLS,
    EXPERIENCE,
}

enum class GuidedField {
    FULL_NAME,
    EMAIL,
    PHONE,
    COURSE,
    COLLEGE,
    EDUCATION_START,
    EDUCATION_END,
    SKILL,
    ROLE,
    EMPLOYER,
    EXPERIENCE_START,
    EXPERIENCE_END,
}

val GUIDED_STEPS: List<GuidedStep> = listOf(
    GuidedStep.CONTACT,
    GuidedStep.EDUCATION,
    GuidedStep.SKILLS,
    GuidedStep.EXPERIENCE,
)

fun GuidedStep.fields(): List<GuidedField> = when (this) {
    GuidedStep.CONTACT -> listOf(
        GuidedField.FULL_NAME,
        GuidedField.EMAIL,
        GuidedField.PHONE,
    )

    GuidedStep.EDUCATION -> listOf(
        GuidedField.COURSE,
        GuidedField.COLLEGE,
        GuidedField.EDUCATION_START,
        GuidedField.EDUCATION_END,
    )

    GuidedStep.SKILLS -> listOf(GuidedField.SKILL)

    GuidedStep.EXPERIENCE -> listOf(
        GuidedField.ROLE,
        GuidedField.EMPLOYER,
        GuidedField.EXPERIENCE_START,
        GuidedField.EXPERIENCE_END,
    )
}

fun GuidedStep.entryCategory(): EntryCategory? = when (this) {
    GuidedStep.EDUCATION -> EntryCategory.EDUCATION
    GuidedStep.EXPERIENCE -> EntryCategory.EXPERIENCE
    GuidedStep.CONTACT, GuidedStep.SKILLS -> null
}

fun guidedStepIndexOf(startStep: String): Int {
    val key = startStep.trim().lowercase()
    val index = GUIDED_STEPS.indexOfFirst { step -> step.name.equals(key, ignoreCase = true) }
    return if (index < 0) 0 else index
}

fun guidedStepAt(index: Int): GuidedStep = GUIDED_STEPS[index.coerceIn(0, GUIDED_STEPS.lastIndex)]

fun skillsOf(values: Map<GuidedField, String>): List<String> =
    values[GuidedField.SKILL].orEmpty()
        .split('\n', ',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }

fun FactDraft.trimmed(): FactDraft = copy(
    title = title.trim(),
    organization = organization.trim(),
    startDate = startDate.trim(),
    endDate = endDate.trim(),
    detail = detail.trim(),
)
