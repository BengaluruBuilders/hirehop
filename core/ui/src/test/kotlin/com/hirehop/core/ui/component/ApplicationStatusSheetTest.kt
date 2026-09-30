package com.hirehop.core.ui.component

import com.hirehop.core.model.ApplicationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplicationStatusSheetTest {

    @Test
    fun offersEveryStatusInTheOrderOfTheSpecification() {
        val options = applicationStatusOptions { status -> status.name }

        assertEquals(
            listOf(
                ApplicationStatus.SAVED,
                ApplicationStatus.APPLIED,
                ApplicationStatus.INTERVIEW,
                ApplicationStatus.OFFER,
                ApplicationStatus.REJECTED,
                ApplicationStatus.NO_RESPONSE,
            ),
            options.map { option -> option.status },
        )
    }

    @Test
    fun labelsEveryOfferedStatus() {
        val options = applicationStatusOptions { status -> status.name.lowercase() }

        assertEquals(listOf("saved", "applied", "interview", "offer", "rejected", "no_response"), options.map { it.label })
    }

    @Test
    fun selectionStartsOnTheCurrentValue() {
        val selection = ApplicationStatusSelection(ApplicationStatus.SAVED)

        assertEquals(ApplicationStatus.SAVED, selection.selected)
        assertEquals(ApplicationStatus.SAVED, selection.confirm())
    }

    @Test
    fun selectionReturnsTheValueTheUserChose() {
        val selection = ApplicationStatusSelection(ApplicationStatus.SAVED)

        selection.select(ApplicationStatus.INTERVIEW)
        selection.select(ApplicationStatus.REJECTED)

        assertEquals(ApplicationStatus.REJECTED, selection.selected)
        assertEquals(ApplicationStatus.REJECTED, selection.confirm())
    }

    @Test
    fun selectionKeepsEveryValueItCanStartFrom() {
        ApplicationStatus.entries.forEach { status ->
            val selection = ApplicationStatusSelection(status)

            assertEquals(status, selection.confirm())
        }
    }
}
