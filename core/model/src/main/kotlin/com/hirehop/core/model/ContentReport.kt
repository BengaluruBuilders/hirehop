package com.hirehop.core.model

import kotlin.time.Instant

enum class ReportedItemKind { REQUIREMENT, RESUME_BULLET, SECTION, COVER_LETTER, PREP_QUESTION }

data class ContentReport(
    val applicationId: String,
    val itemKind: ReportedItemKind,
    val itemId: String,
    val itemText: String,
    val reportedAt: Instant,
    val generationId: String? = null,
)
