package com.hirehop.feature.tailor.impl.export.docx

import com.hirehop.feature.tailor.impl.export.ExportFileName

internal object DocxFileName {

    fun build(name: String, company: String, role: String): String {
        val pdf = ExportFileName.build(name, company, role)
        return pdf.substring(0, pdf.length - PDF_EXTENSION.length) + DOCX_EXTENSION
    }

    private const val PDF_EXTENSION = ".pdf"
    private const val DOCX_EXTENSION = ".docx"
}
