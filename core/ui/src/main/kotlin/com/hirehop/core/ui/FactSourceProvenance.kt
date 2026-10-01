package com.hirehop.core.ui

import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry

class FactSourceProvenance {

    fun kindOf(source: FactSource): HhProvenanceKind =
        when (source) {
            FactSource.IMPORTED -> HhProvenanceKind.Scanned
            FactSource.USER_STATED -> HhProvenanceKind.UserStated
            FactSource.USER_EDITED -> HhProvenanceKind.UserEdited
        }

    fun kindOf(entry: ProfileEntry): HhProvenanceKind = confirmedKindOf(entry.source, entry.isConfirmed)

    fun confirmedKindOf(
        source: FactSource,
        isConfirmed: Boolean = false,
    ): HhProvenanceKind = if (isConfirmed) HhProvenanceKind.Confirmed else kindOf(source)

    fun kindsOf(sources: List<FactSource> = emptyList()): List<HhProvenanceKind> = sources.map { kindOf(it) }

    fun kindsOfEntries(entries: List<ProfileEntry> = emptyList()): List<HhProvenanceKind> =
        entries.map { kindOf(it) }
}
