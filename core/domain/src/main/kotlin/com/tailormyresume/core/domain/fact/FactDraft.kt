package com.tailormyresume.core.domain.fact

import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet

data class FactDraft(
    val category: EntryCategory,
    val title: String,
    val organization: String,
    val startDate: String,
    val endDate: String,
    val detail: String,
    val moreBullets: List<EvidenceBullet> = emptyList(),
)
