package com.hirehop.feature.tailor.impl.export

import com.hirehop.feature.tailor.impl.document.ResumeDocument
import java.io.File

internal interface ResumePdfRenderer {
    suspend fun render(document: ResumeDocument, fileName: String): File
}
