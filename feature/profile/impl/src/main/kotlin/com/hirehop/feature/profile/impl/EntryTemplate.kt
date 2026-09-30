package com.hirehop.feature.profile.impl

import androidx.annotation.StringRes
import com.hirehop.core.model.EntryCategory

internal enum class EntryTemplate(
    val category: EntryCategory,
    @StringRes val labelRes: Int,
) {
    PROJECT(EntryCategory.PROJECT, R.string.feature_profile_impl_add_project),
    INTERNSHIP(EntryCategory.EXPERIENCE, R.string.feature_profile_impl_add_internship),
    COURSE(EntryCategory.CERTIFICATION, R.string.feature_profile_impl_add_course),
    COMPETITION(EntryCategory.ACHIEVEMENT, R.string.feature_profile_impl_add_competition),
    POSITION_OF_RESPONSIBILITY(EntryCategory.EXPERIENCE, R.string.feature_profile_impl_add_position),
}

@StringRes
internal fun EntryCategory.labelRes(): Int = when (this) {
    EntryCategory.EDUCATION -> R.string.feature_profile_impl_category_education
    EntryCategory.EXPERIENCE -> R.string.feature_profile_impl_category_experience
    EntryCategory.PROJECT -> R.string.feature_profile_impl_category_project
    EntryCategory.CERTIFICATION -> R.string.feature_profile_impl_category_certification
    EntryCategory.ACHIEVEMENT -> R.string.feature_profile_impl_category_achievement
}
