package com.hirehop.feature.profile.impl.evidencepath

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.feature.profile.impl.R

@StringRes
internal fun EvidenceCategory.labelRes(): Int = when (this) {
    EvidenceCategory.WORK -> R.string.feature_profile_impl_evidence_path_category_work
    EvidenceCategory.PROJECTS -> R.string.feature_profile_impl_evidence_path_category_projects
    EvidenceCategory.INTERNSHIPS -> R.string.feature_profile_impl_evidence_path_category_internships
    EvidenceCategory.COURSEWORK -> R.string.feature_profile_impl_evidence_path_category_coursework
    EvidenceCategory.COMPETITIONS -> R.string.feature_profile_impl_evidence_path_category_competitions
    EvidenceCategory.POSITIONS -> R.string.feature_profile_impl_evidence_path_category_positions
}

@StringRes
internal fun EvidenceCategory.hintRes(): Int = when (this) {
    EvidenceCategory.WORK -> R.string.feature_profile_impl_evidence_path_hint_work
    EvidenceCategory.PROJECTS -> R.string.feature_profile_impl_evidence_path_hint_projects
    EvidenceCategory.INTERNSHIPS -> R.string.feature_profile_impl_evidence_path_hint_internships
    EvidenceCategory.COURSEWORK -> R.string.feature_profile_impl_evidence_path_hint_coursework
    EvidenceCategory.COMPETITIONS -> R.string.feature_profile_impl_evidence_path_hint_competitions
    EvidenceCategory.POSITIONS -> R.string.feature_profile_impl_evidence_path_hint_positions
}

internal fun EvidenceCategory.icon(): ImageVector = when (this) {
    EvidenceCategory.WORK -> HhIcons.Applications
    EvidenceCategory.PROJECTS -> HhIcons.Facts
    EvidenceCategory.INTERNSHIPS -> HhIcons.Award
    EvidenceCategory.COURSEWORK -> HhIcons.Bookmark
    EvidenceCategory.COMPETITIONS -> HhIcons.Flag
    EvidenceCategory.POSITIONS -> HhIcons.Profile
}

@StringRes
internal fun EvidenceCategory.questionRes(index: Int): Int = when (this) {
    EvidenceCategory.WORK -> R.string.feature_profile_impl_evidence_path_question_work_1
    EvidenceCategory.PROJECTS -> if (index == 0) {
        R.string.feature_profile_impl_evidence_path_question_projects_1
    } else {
        R.string.feature_profile_impl_evidence_path_question_projects_2
    }
    EvidenceCategory.INTERNSHIPS -> R.string.feature_profile_impl_evidence_path_question_internships_1
    EvidenceCategory.COURSEWORK -> R.string.feature_profile_impl_evidence_path_question_coursework_1
    EvidenceCategory.COMPETITIONS -> R.string.feature_profile_impl_evidence_path_question_competitions_1
    EvidenceCategory.POSITIONS -> R.string.feature_profile_impl_evidence_path_question_positions_1
}

@StringRes
internal fun EvidenceFieldProblem.messageRes(): Int = when (this) {
    EvidenceFieldProblem.REQUIRED -> R.string.feature_profile_impl_evidence_path_error_required
    EvidenceFieldProblem.TOO_LONG -> R.string.feature_profile_impl_evidence_path_error_too_long
}
