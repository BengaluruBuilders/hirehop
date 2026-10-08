package com.tailormyresume.feature.tailor.impl.exportpreview

import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.feature.tailor.impl.export.ExportFileName

internal val ExportFormat.wireValue: String get() = name.lowercase()

internal val ExportFormat.extension: String
    get() = when (this) {
        ExportFormat.PDF -> ".pdf"
        ExportFormat.DOCX -> ".docx"
    }

internal fun exportFormatFromWire(wireValue: String): ExportFormat =
    ExportFormat.entries.firstOrNull { format -> format.wireValue.equals(wireValue, ignoreCase = true) }
        ?: ExportFormat.PDF

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
