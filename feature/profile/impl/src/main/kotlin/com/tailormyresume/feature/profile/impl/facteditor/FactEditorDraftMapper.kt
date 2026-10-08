package com.tailormyresume.feature.profile.impl.facteditor

import com.tailormyresume.core.domain.fact.FactDraft
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry

internal fun ProfileEntry.toFactDraft(): FactDraft = FactDraft(
    category = category,
    title = title,
    organization = organization,
    startDate = startDate,
    endDate = endDate,
    detail = bullets.joinToString(separator = DETAIL_SEPARATOR) { it.text }.trim(),
)

internal fun FactDraft.toEntry(
    id: String,
    source: FactSource,
    existingBulletId: String?,
    newBulletId: String,
    isConfirmed: Boolean,
): ProfileEntry = ProfileEntry(
    id = id,
    category = category,
    title = title.trim(),
    organization = organization.trim(),
    startDate = startDate.trim(),
    endDate = endDate.trim(),
    bullets = detail.toBullets(existingBulletId, newBulletId),
    source = source,
    isConfirmed = isConfirmed,
)

private fun String.toBullets(
    existingBulletId: String?,
    newBulletId: String,
): List<EvidenceBullet> {
    val text = trim()
    if (text.isEmpty()) return emptyList()
    return listOf(EvidenceBullet(id = existingBulletId ?: newBulletId, text = text))
}

internal fun categoryOf(entryType: String?): EntryCategory =
    EntryCategory.entries.firstOrNull { it.name.equals(entryType, ignoreCase = true) }
        ?: EntryCategory.PROJECT

private const val DETAIL_SEPARATOR = " "
