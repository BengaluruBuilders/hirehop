package com.hirehop.feature.tailor.impl.export

import com.hirehop.feature.tailor.impl.document.ResumeDocument

internal interface ResumePdfRenderer {
    suspend fun render(document: ResumeDocument, fileName: String): RenderedResume
}
