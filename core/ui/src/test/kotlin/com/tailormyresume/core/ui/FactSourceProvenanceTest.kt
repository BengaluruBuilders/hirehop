package com.tailormyresume.core.ui

import com.tailormyresume.core.designsystem.component.TmrProvenanceKind
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class FactSourceProvenanceTest {

    private val provenance = FactSourceProvenance()

    @Test
    fun mapsEverySourceToAGenericKind() {
        assertEquals(TmrProvenanceKind.Scanned, provenance.kindOf(FactSource.IMPORTED))
        assertEquals(TmrProvenanceKind.UserStated, provenance.kindOf(FactSource.USER_STATED))
        assertEquals(TmrProvenanceKind.UserEdited, provenance.kindOf(FactSource.USER_EDITED))
    }

    @Test
    fun confirmationOutranksTheSource() {
        FactSource.entries.forEach { source ->
            assertEquals(TmrProvenanceKind.Confirmed, provenance.confirmedKindOf(source, isConfirmed = true))
            assertEquals(provenance.kindOf(source), provenance.confirmedKindOf(source, isConfirmed = false))
        }
    }

    @Test
    fun anUnconfirmedEntryKeepsItsSourceKind() {
        val entry = profileEntry(source = FactSource.USER_STATED, isConfirmed = false)
        assertEquals(TmrProvenanceKind.UserStated, provenance.kindOf(entry))
    }

    @Test
    fun aConfirmedEntryBecomesConfirmed() {
        val entry = profileEntry(source = FactSource.IMPORTED, isConfirmed = true)
        assertEquals(TmrProvenanceKind.Confirmed, provenance.kindOf(entry))
    }

    @Test
    fun mapsAListOfEntriesInOrder() {
        val entries = listOf(
            profileEntry(source = FactSource.IMPORTED, isConfirmed = true),
            profileEntry(source = FactSource.USER_STATED, isConfirmed = false),
            profileEntry(source = FactSource.USER_EDITED, isConfirmed = false),
        )
        assertEquals(
            listOf(
                TmrProvenanceKind.Confirmed,
                TmrProvenanceKind.UserStated,
                TmrProvenanceKind.UserEdited,
            ),
            provenance.kindsOfEntries(entries),
        )
    }

    @Test
    fun mapsAListOfSourcesWithoutConfirmation() {
        val sources = listOf(FactSource.IMPORTED, FactSource.USER_STATED)
        assertEquals(
            listOf(TmrProvenanceKind.Scanned, TmrProvenanceKind.UserStated),
            provenance.kindsOf(sources),
        )
    }

    private fun profileEntry(
        source: FactSource,
        isConfirmed: Boolean,
    ) = ProfileEntry(
        id = "P-01",
        category = EntryCategory.EXPERIENCE,
        title = "Associate Analyst",
        organization = "Northwind GCC",
        startDate = "2023",
        endDate = "2026",
        bullets = emptyList(),
        source = source,
        isConfirmed = isConfirmed,
    )
}
