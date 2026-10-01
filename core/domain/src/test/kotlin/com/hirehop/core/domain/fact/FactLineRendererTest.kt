package com.hirehop.core.domain.fact

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import org.junit.Test

class FactLineRendererTest {
    private val draft = FactDraft(
        category = EntryCategory.PROJECT,
        title = "Placement Stats Dashboard",
        organization = "College placement cell",
        startDate = "2025-06",
        endDate = "2025-11",
        detail = "Built a dashboard of 3 batches of placement data.",
    )

    @Test
    fun everyPresentFieldReadsInOrder() {
        assertThat(FactLineRenderer.render(draft)).isEqualTo(
            "Placement Stats Dashboard · Built a dashboard of 3 batches of placement data. · " +
                "College placement cell · 2025-06 to 2025-11",
        )
    }

    @Test
    fun blankOrganizationIsOmittedWithoutAPlaceholder() {
        val line = FactLineRenderer.render(draft.copy(organization = "   "))

        assertThat(line).isEqualTo(
            "Placement Stats Dashboard · Built a dashboard of 3 batches of placement data. · 2025-06 to 2025-11",
        )
    }

    @Test
    fun blankDetailIsOmittedWithoutAPlaceholder() {
        val line = FactLineRenderer.render(draft.copy(detail = ""))

        assertThat(line).isEqualTo("Placement Stats Dashboard · College placement cell · 2025-06 to 2025-11")
    }

    @Test
    fun blankDatesAreOmitted() {
        val line = FactLineRenderer.render(draft.copy(startDate = "", endDate = "  "))

        assertThat(line).isEqualTo(
            "Placement Stats Dashboard · Built a dashboard of 3 batches of placement data. · College placement cell",
        )
    }

    @Test
    fun aSingleDateReadsWithoutTheWordTo() {
        assertThat(FactLineRenderer.render(draft.copy(endDate = ""))).endsWith("College placement cell · 2025-06")
    }

    @Test
    fun anEmptyDraftRendersNothing() {
        val empty = draft.copy(title = "", organization = "", startDate = "", endDate = "", detail = "")

        assertThat(FactLineRenderer.render(empty)).isEmpty()
    }

    @Test
    fun aTitleOnlyDraftRendersOnlyTheTitle() {
        val titleOnly = draft.copy(organization = "", startDate = "", endDate = "", detail = "")

        assertThat(FactLineRenderer.render(titleOnly)).isEqualTo("Placement Stats Dashboard")
    }

    @Test
    fun surroundingSpacesAreTrimmedAway() {
        val padded = draft.copy(title = "  Padded  ", detail = "  detail  ")

        assertThat(FactLineRenderer.render(padded)).startsWith("Padded · detail ·")
    }

    @Test
    fun theLineNeverUsesWordsTheDraftDoesNotHave() {
        val words = FactLineRenderer.render(draft).split(" ").filter { it.isNotBlank() && it != "·" }
        val known = listOf(draft.title, draft.organization, draft.startDate, draft.endDate, draft.detail, "to")

        assertThat(words.filter { word -> known.none { it.contains(word) } }).isEmpty()
    }

    @Test
    fun theLineNeverAddsANumberThatIsNotInTheDraft() {
        val withoutNumber = draft.copy(detail = "Built a dashboard of placement data.")

        val line = FactLineRenderer.render(withoutNumber)

        assertThat(line).doesNotContain("3")
        assertThat(line).contains("Built a dashboard of placement data.")
    }

    @Test
    fun anEntryRendersFromItsFirstBullet() {
        val entry = entryOf(
            title = "Android developer intern",
            organization = "Kitebox Software, Pune",
            startDate = "2025-06",
            endDate = "2025-11",
            bullets = listOf("Wrote Android screens in Kotlin with Jetpack Compose."),
        )

        assertThat(FactLineRenderer.render(entry)).isEqualTo(
            "Android developer intern · Wrote Android screens in Kotlin with Jetpack Compose. · " +
                "Kitebox Software, Pune · 2025-06 to 2025-11",
        )
    }

    @Test
    fun anEntryWithoutBulletsOmitsTheDetail() {
        val entry = entryOf(
            title = "Skills",
            organization = "Priya Deshmukh",
            startDate = "",
            endDate = "",
            bullets = emptyList(),
        )

        assertThat(FactLineRenderer.render(entry)).isEqualTo("Skills · Priya Deshmukh")
    }

    @Test
    fun aBlankEntryRendersNothing() {
        val entry = entryOf(title = "", organization = "", startDate = "", endDate = "", bullets = emptyList())

        assertThat(FactLineRenderer.render(entry)).isEmpty()
    }

    @Test
    fun anEntryNeverUsesWordsTheEntryDoesNotHave() {
        val entry = entryOf(
            title = "Coding club lead",
            organization = "Department coding club",
            startDate = "2024",
            endDate = "2025",
            bullets = listOf("Ran weekly Android study sessions."),
        )

        val words = FactLineRenderer.render(entry).split(" ").filter { it.isNotBlank() && it != "·" }
        val known = listOf(
            entry.title,
            entry.organization,
            entry.startDate,
            entry.endDate,
            entry.bullets.first().text,
            "to",
        )

        assertThat(words.filter { word -> known.none { it.contains(word) } }).isEmpty()
    }

    private fun entryOf(
        title: String,
        organization: String,
        startDate: String,
        endDate: String,
        bullets: List<String>,
    ) = ProfileEntry(
        id = "I-01",
        category = EntryCategory.EXPERIENCE,
        title = title,
        organization = organization,
        startDate = startDate,
        endDate = endDate,
        bullets = bullets.mapIndexed { index, text -> EvidenceBullet("I-01-b${index + 1}", text) },
        source = FactSource.IMPORTED,
        isConfirmed = true,
    )
}
