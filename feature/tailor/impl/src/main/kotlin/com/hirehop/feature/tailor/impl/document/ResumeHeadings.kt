package com.hirehop.feature.tailor.impl.document

import android.content.Context
import com.hirehop.core.model.EntryCategory
import com.hirehop.feature.tailor.impl.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface ResumeHeadings {
    fun forCategory(category: EntryCategory): String

    val skills: String
}

internal class AndroidResumeHeadings @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ResumeHeadings {

    override fun forCategory(category: EntryCategory): String = context.getString(
        when (category) {
            EntryCategory.EDUCATION -> R.string.feature_tailor_impl_document_heading_education
            EntryCategory.EXPERIENCE -> R.string.feature_tailor_impl_document_heading_experience
            EntryCategory.PROJECT -> R.string.feature_tailor_impl_document_heading_projects
            EntryCategory.CERTIFICATION -> R.string.feature_tailor_impl_document_heading_certifications
            EntryCategory.ACHIEVEMENT -> R.string.feature_tailor_impl_document_heading_achievements
        },
    )

    override val skills: String
        get() = context.getString(R.string.feature_tailor_impl_document_heading_skills)
}
