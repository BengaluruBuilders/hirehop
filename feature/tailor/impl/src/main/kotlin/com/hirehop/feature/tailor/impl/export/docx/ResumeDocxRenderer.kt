package com.hirehop.feature.tailor.impl.export.docx

import com.hirehop.feature.tailor.impl.document.ResumeDocument
import java.io.File

internal interface ResumeDocxRenderer {
    suspend fun render(document: ResumeDocument, fileName: String): File
}
