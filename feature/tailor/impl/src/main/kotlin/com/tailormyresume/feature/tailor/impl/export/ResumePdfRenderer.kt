package com.tailormyresume.feature.tailor.impl.export

import com.tailormyresume.feature.tailor.impl.document.ResumeDocument

internal interface ResumePdfRenderer {
    suspend fun render(document: ResumeDocument, fileName: String): RenderedResume
}
