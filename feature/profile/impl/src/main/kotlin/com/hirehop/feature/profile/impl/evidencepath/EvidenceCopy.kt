package com.hirehop.feature.profile.impl.evidencepath

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hirehop.core.model.EntryCategory
import com.hirehop.feature.profile.impl.R

@StringRes
internal fun categoryLabelRes(category: EvidenceCategory): Int = when (category) {
    EvidenceCategory.PROJECTS -> R.string.feature_profile_impl_evidence_path_category_projects
    EvidenceCategory.INTERNSHIPS -> R.string.feature_profile_impl_evidence_path_category_internships
    EvidenceCategory.COURSEWORK -> R.string.feature_profile_impl_evidence_path_category_coursework
    EvidenceCategory.COMPETITIONS -> R.string.feature_profile_impl_evidence_path_category_competitions
    EvidenceCategory.POSITIONS -> R.string.feature_profile_impl_evidence_path_category_positions
}

@StringRes
internal fun categoryQuestionRes(category: EvidenceCategory): Int = when (category) {
    EvidenceCategory.PROJECTS -> R.string.feature_profile_impl_evidence_path_question_projects_name
    EvidenceCategory.INTERNSHIPS -> R.string.feature_profile_impl_evidence_path_question_internships_name
    EvidenceCategory.COURSEWORK -> R.string.feature_profile_impl_evidence_path_question_coursework_name
    EvidenceCategory.COMPETITIONS -> R.string.feature_profile_impl_evidence_path_question_competitions_name
    EvidenceCategory.POSITIONS -> R.string.feature_profile_impl_evidence_path_question_positions_name
}

@StringRes
internal fun titleLabelRes(category: EvidenceCategory): Int = when (category) {
    EvidenceCategory.PROJECTS -> R.string.feature_profile_impl_evidence_path_label_projects_title
    EvidenceCategory.INTERNSHIPS -> R.string.feature_profile_impl_evidence_path_label_internships_title
    EvidenceCategory.COURSEWORK -> R.string.feature_profile_impl_evidence_path_label_coursework_title
    EvidenceCategory.COMPETITIONS -> R.string.feature_profile_impl_evidence_path_label_competitions_title
    EvidenceCategory.POSITIONS -> R.string.feature_profile_impl_evidence_path_label_positions_title
}

@StringRes
internal fun exampleRes(category: EvidenceCategory): Int = when (category) {
    EvidenceCategory.PROJECTS -> R.string.feature_profile_impl_evidence_path_example_projects
    EvidenceCategory.INTERNSHIPS -> R.string.feature_profile_impl_evidence_path_example_internships
    EvidenceCategory.COURSEWORK -> R.string.feature_profile_impl_evidence_path_example_coursework
    EvidenceCategory.COMPETITIONS -> R.string.feature_profile_impl_evidence_path_example_competitions
    EvidenceCategory.POSITIONS -> R.string.feature_profile_impl_evidence_path_example_positions
}

@StringRes
internal fun promptLabelRes(
    prompt: EvidencePrompt,
    category: EvidenceCategory,
): Int = when (prompt) {
    EvidencePrompt.TITLE -> titleLabelRes(category)
    EvidencePrompt.DETAIL -> R.string.feature_profile_impl_evidence_path_label_detail
    EvidencePrompt.ORGANIZATION -> R.string.feature_profile_impl_evidence_path_label_organization
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
internal fun problemText(problem: EvidenceFieldProblem): String = stringResource(
    when (problem) {
        EvidenceFieldProblem.REQUIRED -> R.string.feature_profile_impl_evidence_path_error_required
        EvidenceFieldProblem.END_BEFORE_START -> R.string.feature_profile_impl_evidence_path_error_end_before_start
        EvidenceFieldProblem.TOO_LONG -> R.string.feature_profile_impl_evidence_path_error_too_long
    },
)

@Composable
internal fun messageText(message: EvidenceMessage): String = stringResource(
    when (message) {
        EvidenceMessage.LOAD_FAILED -> R.string.feature_profile_impl_evidence_path_load_failed
        EvidenceMessage.SAVED -> R.string.feature_profile_impl_evidence_path_saved_message
        EvidenceMessage.OFFLINE_QUEUED -> R.string.feature_profile_impl_evidence_path_offline_queued_message
        EvidenceMessage.SAVE_REJECTED -> R.string.feature_profile_impl_evidence_path_save_rejected_body
    },
)
