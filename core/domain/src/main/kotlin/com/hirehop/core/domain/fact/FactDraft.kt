package com.hirehop.core.domain.fact

import com.hirehop.core.model.EntryCategory

data class FactDraft(
    val category: EntryCategory,
    val title: String,
    val organization: String,
    val startDate: String,
    val endDate: String,
    val detail: String,
)
