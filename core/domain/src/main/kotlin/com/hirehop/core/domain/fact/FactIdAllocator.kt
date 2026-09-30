package com.hirehop.core.domain.fact

import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.ProfileEntry
import javax.inject.Inject

class FactIdAllocator @Inject constructor() {
    fun nextId(category: EntryCategory, existing: Collection<ProfileEntry>): String {
        val prefix = prefixOf(category)
        val taken = existing.map { it.id }.toSet()
        var index = 1
        while (idOf(prefix, index) in taken) {
            index++
        }
        return idOf(prefix, index)
    }

    private fun idOf(prefix: String, index: Int): String = prefix + "-" + index.toString().padStart(2, '0')

    private fun prefixOf(category: EntryCategory): String = when (category) {
        EntryCategory.EDUCATION -> "U"
        EntryCategory.EXPERIENCE -> "I"
        EntryCategory.PROJECT -> "C"
        EntryCategory.CERTIFICATION -> "X"
        EntryCategory.ACHIEVEMENT -> "P"
    }
}
