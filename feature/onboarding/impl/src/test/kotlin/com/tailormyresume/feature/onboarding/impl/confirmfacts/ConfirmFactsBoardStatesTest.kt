package com.tailormyresume.feature.onboarding.impl.confirmfacts

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import org.junit.Test

class ConfirmFactsBoardStatesTest {
    private val profile = CandidateProfile(
        fullName = "Asha Rao",
        email = "asha.rao@example.com",
        phone = "+91 90000 00000",
        headline = "",
        skills = listOf("SQL"),
        entries = listOf(
            ProfileEntry(
                id = "W-01",
                category = EntryCategory.EXPERIENCE,
                title = "Data Operations Associate",
                organization = "Pune",
                startDate = "Jul 2025",
                endDate = "now",
                bullets = listOf(EvidenceBullet(id = "W-01-b1", text = "Built weekly sales reports.")),
                source = FactSource.IMPORTED,
                isConfirmed = false,
            ),
            ProfileEntry(
                id = "W-02",
                category = EntryCategory.PROJECT,
                title = "Dashboard",
                organization = "Power BI",
                startDate = "2024",
                endDate = "2024",
                bullets = listOf(EvidenceBullet(id = "W-02-b1", text = "Placement data.")),
                source = FactSource.IMPORTED,
                isConfirmed = false,
            ),
        ),
    )

    private fun loaded() = ConfirmFactsScenarioMapper.withProfile(
        state = ConfirmFactsScenarioMapper.seed(DebugScenario.ERROR),
        profile = profile,
        scenario = DebugScenario.ERROR,
    )

    @Test
    fun theErrorBoardStateHasAFailedSaveAndStillListsTheFacts() {
        val state = loaded().withSaveFailed()

        assertThat(state.hasSaveFailed).isTrue()
        assertThat(state.facts).isNotEmpty()
    }

    @Test
    fun theEmptySectionBoardStateShowsAnEmptySectionCardAlongsidePendingFacts() {
        val state = emptySectionState(profile)

        assertThat(state.isFullyConfirmed).isFalse()
        assertThat(state.visibleSections.any { it.isEmpty }).isTrue()
        assertThat(state.facts).isNotEmpty()
    }
}
