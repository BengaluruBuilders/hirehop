package com.hirehop.feature.profile.impl.guidedform

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hirehop.core.model.EntryCategory
import com.hirehop.feature.profile.impl.R

@StringRes
internal fun stepTitleRes(step: GuidedStep): Int = when (step) {
    GuidedStep.CONTACT -> R.string.feature_profile_impl_guided_form_step_contact
    GuidedStep.EDUCATION -> R.string.feature_profile_impl_guided_form_step_education
    GuidedStep.SKILLS -> R.string.feature_profile_impl_guided_form_step_skills
    GuidedStep.EXPERIENCE -> R.string.feature_profile_impl_guided_form_step_experience
}

@StringRes
internal fun stepSubtitleRes(step: GuidedStep): Int = when (step) {
    GuidedStep.CONTACT -> R.string.feature_profile_impl_guided_form_step_contact_subtitle
    GuidedStep.EDUCATION -> R.string.feature_profile_impl_guided_form_step_education_subtitle
    GuidedStep.SKILLS -> R.string.feature_profile_impl_guided_form_step_skills_subtitle
    GuidedStep.EXPERIENCE -> R.string.feature_profile_impl_guided_form_step_experience_subtitle
}

@StringRes
internal fun fieldLabelRes(field: GuidedField): Int = when (field) {
    GuidedField.FULL_NAME -> R.string.feature_profile_impl_guided_form_field_full_name
    GuidedField.EMAIL -> R.string.feature_profile_impl_guided_form_field_email
    GuidedField.PHONE -> R.string.feature_profile_impl_guided_form_field_phone
    GuidedField.COURSE -> R.string.feature_profile_impl_guided_form_field_course
    GuidedField.COLLEGE -> R.string.feature_profile_impl_guided_form_field_college
    GuidedField.ROLE -> R.string.feature_profile_impl_guided_form_field_role
    GuidedField.EMPLOYER -> R.string.feature_profile_impl_guided_form_field_employer
    GuidedField.SKILL -> R.string.feature_profile_impl_guided_form_skills_title
    GuidedField.EDUCATION_START, GuidedField.EXPERIENCE_START ->
        R.string.feature_profile_impl_guided_form_field_start

    GuidedField.EDUCATION_END, GuidedField.EXPERIENCE_END ->
        R.string.feature_profile_impl_guided_form_field_end
}

@StringRes
internal fun sectionLabelRes(category: EntryCategory): Int = when (category) {
    EntryCategory.EDUCATION -> R.string.feature_profile_impl_guided_form_section_education
    EntryCategory.EXPERIENCE -> R.string.feature_profile_impl_guided_form_section_experience
    EntryCategory.PROJECT -> R.string.feature_profile_impl_guided_form_section_projects
    EntryCategory.CERTIFICATION -> R.string.feature_profile_impl_guided_form_section_certifications
    EntryCategory.ACHIEVEMENT -> R.string.feature_profile_impl_guided_form_section_extras
}

@Composable
internal fun problemText(problem: GuidedFieldProblem): String = stringResource(
    when (problem) {
        GuidedFieldProblem.REQUIRED -> R.string.feature_profile_impl_guided_form_error_required
        GuidedFieldProblem.END_BEFORE_START -> R.string.feature_profile_impl_guided_form_error_end_before_start
        GuidedFieldProblem.TOO_LONG -> R.string.feature_profile_impl_guided_form_error_too_long
    },
)

@Composable
internal fun messageText(message: GuidedMessage): String = stringResource(
    when (message) {
        GuidedMessage.LOAD_FAILED -> R.string.feature_profile_impl_guided_form_load_failed
        GuidedMessage.SAVED -> R.string.feature_profile_impl_guided_form_saved_message
        GuidedMessage.OFFLINE_QUEUED -> R.string.feature_profile_impl_guided_form_offline_queued_message
        GuidedMessage.SAVE_REJECTED -> R.string.feature_profile_impl_guided_form_save_rejected_body
    },
)
