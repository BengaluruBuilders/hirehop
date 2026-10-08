package com.tailormyresume.core.domain.fact

import com.tailormyresume.core.model.ProfileEntry

object FactDisplayIds {
    const val LEGACY_USER_STATED_ENTRY_ID = "user-stated"

    private val designId = Regex("^[A-Z]{1,2}-\\d{2,}$")

    fun of(entry: ProfileEntry, entries: List<ProfileEntry>): String {
        if (designId.matches(entry.id)) return entry.id
        val prefix = prefixOf(entry)
        val taken = entries.map { it.id }.filter { designId.matches(it) }.toSet()
        val legacy = entries.filter { !designId.matches(it.id) && prefixOf(it) == prefix }
        val position = legacy.indexOfFirst { it.id == entry.id }.coerceAtLeast(0)
        return generateSequence(1) { it + 1 }
            .map { prefix + "-" + it.toString().padStart(2, '0') }
            .filter { it !in taken }
            .elementAt(position)
    }

    private fun prefixOf(entry: ProfileEntry): String =
        if (entry.id == LEGACY_USER_STATED_ENTRY_ID) {
            FactIdPrefix.UNCATEGORISED
        } else {
            FactIdPrefix.of(entry.category, entry.title)
        }

    fun forEntryId(entryId: String, entries: List<ProfileEntry>): String? =
        entries.firstOrNull { it.id == entryId }?.let { of(it, entries) }

    fun forBulletId(bulletId: String, entries: List<ProfileEntry>): String? =
        entries.firstOrNull { entry -> entry.bullets.any { it.id == bulletId } }?.let { of(it, entries) }

    fun forIds(ids: Collection<String>, entries: List<ProfileEntry>): List<String> =
        ids.mapNotNull { forBulletId(it, entries) ?: forEntryId(it, entries) }.distinct()
}
