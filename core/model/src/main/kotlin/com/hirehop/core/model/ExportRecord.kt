package com.hirehop.core.model

import kotlin.time.Instant

enum class ExportFormat { PDF, DOCX }

enum class CreditKind { FREE, PURCHASED }

data class ExportRecord(
    val applicationId: String,
    val format: ExportFormat,
    val fileName: String,
    val exportedAt: Instant,
    val creditKind: CreditKind?,
    val pageCount: Int? = null,
    val templateName: String? = null,
)
