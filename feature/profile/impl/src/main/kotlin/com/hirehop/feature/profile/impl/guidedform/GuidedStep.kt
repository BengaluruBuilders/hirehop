package com.hirehop.feature.profile.impl.guidedform

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
    EDUCATION_END,
    COURSEWORK,
    SKILL,
}

val GUIDED_STEPS: List<GuidedStep> = GuidedStep.entries

fun GuidedStep.fields(): List<GuidedField> = when (this) {
    GuidedStep.CONTACT -> listOf(GuidedField.FULL_NAME, GuidedField.EMAIL, GuidedField.PHONE)
    GuidedStep.EDUCATION -> listOf(
        GuidedField.COURSE,
        GuidedField.COLLEGE,
        GuidedField.EDUCATION_END,
        GuidedField.COURSEWORK,
    )
    GuidedStep.SKILLS -> listOf(GuidedField.SKILL)
    GuidedStep.EXPERIENCE -> emptyList()
}

fun guidedStepIndexOf(startStep: String): Int {
    val index = GUIDED_STEPS.indexOfFirst { it.name.equals(startStep.trim(), ignoreCase = true) }
    return if (index < 0) 0 else index
}

fun guidedStepAt(index: Int): GuidedStep = GUIDED_STEPS[index.coerceIn(0, GUIDED_STEPS.lastIndex)]
