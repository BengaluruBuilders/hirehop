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
    detail = bullets.firstOrNull()?.text.orEmpty(),
    moreBullets = bullets.drop(1),
)

internal fun FactDraft.toEntry(
    id: String,
    source: FactSource,
    existingBullet: EvidenceBullet?,
    newBulletId: String,
    isConfirmed: Boolean,
): ProfileEntry = ProfileEntry(
    id = id,
    category = category,
    title = title.trim(),
    organization = organization.trim(),
    startDate = startDate.trim(),
    endDate = endDate.trim(),
    bullets = toBullets(existingBullet, newBulletId),
    source = source,
    isConfirmed = isConfirmed,
)

private fun FactDraft.toBullets(
    existingBullet: EvidenceBullet?,
    newBulletId: String,
): List<EvidenceBullet> {
    val text = detail.trim()
    val first = when {
        text.isEmpty() -> null
        existingBullet != null && existingBullet.text.trim() == text -> existingBullet
        else -> EvidenceBullet(id = existingBullet?.id ?: newBulletId, text = text)
    }
    return listOfNotNull(first) + moreBullets.filter { it.text.isNotBlank() }
}

internal fun categoryOf(entryType: String?): EntryCategory =
    EntryCategory.entries.firstOrNull { it.name.equals(entryType, ignoreCase = true) }
        ?: EntryCategory.PROJECT
