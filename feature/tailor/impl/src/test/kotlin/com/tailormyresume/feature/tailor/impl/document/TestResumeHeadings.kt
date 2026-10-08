package com.tailormyresume.feature.tailor.impl.document

import com.tailormyresume.core.model.EntryCategory

internal object TestResumeHeadings : ResumeHeadings {
    override fun forCategory(category: EntryCategory): String = "Heading ${category.name.lowercase()}"

    override val skills: String = "Heading skills"
}
