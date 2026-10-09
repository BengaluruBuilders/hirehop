package com.tailormyresume.feature.profile.impl.guidedform

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tailormyresume.feature.profile.impl.R

@StringRes
internal fun stepTitleRes(step: GuidedStep): Int = when (step) {
    GuidedStep.CONTACT -> R.string.feature_profile_impl_guided_form_step_contact
    GuidedStep.EDUCATION -> R.string.feature_profile_impl_guided_form_step_education
    GuidedStep.SKILLS -> R.string.feature_profile_impl_guided_form_step_skills
    GuidedStep.EXPERIENCE -> R.string.feature_profile_impl_guided_form_step_experience
}

@StringRes
internal fun fieldLabelRes(field: GuidedField): Int = when (field) {
    GuidedField.FULL_NAME -> R.string.feature_profile_impl_guided_form_field_full_name
    GuidedField.EMAIL -> R.string.feature_profile_impl_guided_form_field_email
    GuidedField.PHONE -> R.string.feature_profile_impl_guided_form_field_phone
    GuidedField.COURSE -> R.string.feature_profile_impl_guided_form_field_course
    GuidedField.COLLEGE -> R.string.feature_profile_impl_guided_form_field_college
    GuidedField.EDUCATION_END -> R.string.feature_profile_impl_guided_form_field_year
    GuidedField.COURSEWORK -> R.string.feature_profile_impl_guided_form_field_coursework
    GuidedField.SKILL -> R.string.feature_profile_impl_guided_form_field_skill
}

@Composable
internal fun problemText(problem: GuidedFieldProblem): String = stringResource(
    when (problem) {
        GuidedFieldProblem.REQUIRED -> R.string.feature_profile_impl_guided_form_error_required
        GuidedFieldProblem.END_BEFORE_START -> R.string.feature_profile_impl_guided_form_error_end_before_start
        GuidedFieldProblem.TOO_LONG -> R.string.feature_profile_impl_guided_form_error_too_long
        GuidedFieldProblem.INVALID_DATE -> R.string.feature_profile_impl_guided_form_error_invalid_date
        GuidedFieldProblem.INVALID_EMAIL -> R.string.feature_profile_impl_guided_form_error_invalid_email
        GuidedFieldProblem.INVALID_PHONE -> R.string.feature_profile_impl_guided_form_error_invalid_phone
    },
)
