package com.tailormyresume.core.ui

import com.tailormyresume.core.designsystem.component.TmrProvenanceKind
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry

class FactSourceProvenance {

    fun kindOf(source: FactSource): TmrProvenanceKind =
        when (source) {
            FactSource.IMPORTED -> TmrProvenanceKind.Scanned
            FactSource.USER_STATED -> TmrProvenanceKind.UserStated
            FactSource.USER_EDITED -> TmrProvenanceKind.UserEdited
        }

    fun kindOf(entry: ProfileEntry): TmrProvenanceKind = confirmedKindOf(entry.source, entry.isConfirmed)

    fun confirmedKindOf(
        source: FactSource,
        isConfirmed: Boolean = false,
    ): TmrProvenanceKind = if (isConfirmed) TmrProvenanceKind.Confirmed else kindOf(source)

    fun kindsOf(sources: List<FactSource> = emptyList()): List<TmrProvenanceKind> = sources.map { kindOf(it) }

    fun kindsOfEntries(entries: List<ProfileEntry> = emptyList()): List<TmrProvenanceKind> =
        entries.map { kindOf(it) }
}
