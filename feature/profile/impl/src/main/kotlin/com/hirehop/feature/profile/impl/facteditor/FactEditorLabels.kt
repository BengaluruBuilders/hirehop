package com.hirehop.feature.profile.impl.facteditor

import androidx.annotation.StringRes
import com.hirehop.core.model.EntryCategory
import com.hirehop.feature.profile.impl.R

@StringRes
internal fun EntryCategory.nameRes(): Int = when (this) {
    EntryCategory.EDUCATION -> R.string.feature_profile_impl_fact_editor_category_education
    EntryCategory.EXPERIENCE -> R.string.feature_profile_impl_fact_editor_category_experience
    EntryCategory.PROJECT -> R.string.feature_profile_impl_fact_editor_category_project
    EntryCategory.CERTIFICATION -> R.string.feature_profile_impl_fact_editor_category_certification
    EntryCategory.ACHIEVEMENT -> R.string.feature_profile_impl_fact_editor_category_achievement
}

@StringRes
internal fun EntryCategory.titleLabelRes(): Int = when (this) {
    EntryCategory.EDUCATION -> R.string.feature_profile_impl_fact_editor_title_label_education
    EntryCategory.EXPERIENCE -> R.string.feature_profile_impl_fact_editor_title_label_experience
    EntryCategory.PROJECT -> R.string.feature_profile_impl_fact_editor_field_title
    EntryCategory.CERTIFICATION -> R.string.feature_profile_impl_fact_editor_title_label_certification
    EntryCategory.ACHIEVEMENT -> R.string.feature_profile_impl_fact_editor_title_label_achievement
}

@StringRes
internal fun EntryCategory.titlePlaceholderRes(): Int = when (this) {
    EntryCategory.EDUCATION -> R.string.feature_profile_impl_fact_editor_title_hint_education
    EntryCategory.EXPERIENCE -> R.string.feature_profile_impl_fact_editor_title_hint_experience
    EntryCategory.PROJECT -> R.string.feature_profile_impl_fact_editor_field_title_placeholder
    EntryCategory.CERTIFICATION -> R.string.feature_profile_impl_fact_editor_title_hint_certification
    EntryCategory.ACHIEVEMENT -> R.string.feature_profile_impl_fact_editor_title_hint_achievement
}

@StringRes
internal fun EntryCategory.detailLabelRes(): Int = when (this) {
    EntryCategory.EDUCATION, EntryCategory.CERTIFICATION -> R.string.feature_profile_impl_fact_editor_detail_label_details
    EntryCategory.EXPERIENCE, EntryCategory.PROJECT, EntryCategory.ACHIEVEMENT ->
        R.string.feature_profile_impl_fact_editor_field_detail
}

@StringRes
internal fun EntryCategory.organizationLabelRes(): Int = when (this) {
    EntryCategory.EDUCATION -> R.string.feature_profile_impl_fact_editor_organization_education
    EntryCategory.EXPERIENCE -> R.string.feature_profile_impl_fact_editor_organization_experience
    EntryCategory.PROJECT -> R.string.feature_profile_impl_fact_editor_field_tools
    EntryCategory.CERTIFICATION -> R.string.feature_profile_impl_fact_editor_organization_certification
    EntryCategory.ACHIEVEMENT -> R.string.feature_profile_impl_fact_editor_organization_achievement
}
