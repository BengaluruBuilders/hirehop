package com.hirehop.feature.tailor.impl.exportpreview

import com.hirehop.feature.tailor.impl.export.ExportFileName

internal enum class ExportFormat(val wireValue: String, val extension: String) {
    PDF("pdf", ".pdf"),
    DOCX("docx", ".docx"),
    ;

    val isLaterRelease: Boolean get() = this == DOCX

    companion object {
        fun fromWire(wireValue: String): ExportFormat =
            entries.firstOrNull { format -> format.wireValue.equals(wireValue, ignoreCase = true) } ?: PDF
    }
}

internal object ExportFileNames {

    fun build(format: ExportFormat, name: String, company: String, role: String): String {
        val base = ExportFileName.build(name = name, company = company, role = role)
        return when (format) {
            ExportFormat.PDF -> base
            ExportFormat.DOCX -> base.removeSuffix(PDF_EXTENSION) + format.extension
        }
    }

    private const val PDF_EXTENSION = ".pdf"
}
