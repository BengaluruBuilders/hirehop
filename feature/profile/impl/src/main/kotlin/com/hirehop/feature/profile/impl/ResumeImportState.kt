package com.hirehop.feature.profile.impl

import com.hirehop.core.model.CandidateProfile

data class ResumeImportState(
    val rawText: String = "",
    val preview: CandidateProfile? = null,
    val isParsing: Boolean = false,
) {
    val canParse: Boolean get() = rawText.isNotBlank() && !isParsing

    val previewHasContent: Boolean
        get() = preview != null && (preview.fullName.isNotBlank() || preview.entries.isNotEmpty())
}
