package com.tailormyresume.feature.onboarding.impl.confirmfacts

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.testing.data.sampleProfile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h2400dp-xxhdpi")
class ConfirmFactsPartlyConfirmedTooManyLinesTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val confirmedLegacy = ProfileEntry(
        id = "LEGACY",
        category = EntryCategory.EXPERIENCE,
        title = "Legacy analyst",
        organization = "Pune",
        startDate = "May 2020",
        endDate = "Jul 2021",
        bullets = (1..18).map { EvidenceBullet("LEGACY-b$it", "Did task $it.") },
        source = FactSource.IMPORTED,
        isConfirmed = true,
    )

    private val state = ConfirmFactsScenarioMapper.withProfile(
        state = ConfirmFactsScenarioMapper.seed(DebugScenario.PARTLY_CONFIRMED),
        profile = sampleProfile.copy(
            entries = listOf(confirmedLegacy) + sampleProfile.entries.filter { it.category != EntryCategory.EXPERIENCE }.take(2),
        ),
        scenario = DebugScenario.PARTLY_CONFIRMED,
    )

    @Test
    fun confirmedEntryWithTooManyLinesShowsInTheFlaggedBlockAndIsNotCountedAsConfirmed() {
        val legacy = state.facts.first { it.id == "LEGACY" }
        assertThat(legacy.isConfirmed).isTrue()
        assertThat(legacy.hasTooManyBullets).isTrue()
        assertThat(legacy.hasTooLongBullet).isFalse()
        assertThat(state.facts.count { it.isConfirmedWithinLimits }).isEqualTo(0)

        show()

        composeRule.onNodeWithText(legacy.title, substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Lines after the first 15 are left out", substring = true).assertExists()
        composeRule.onNodeWithText("1 fact confirmed").assertDoesNotExist()
    }

    @Test
    fun theFlaggedEntryFollowsItsSectionChip() {
        val legacy = state.facts.first { it.id == "LEGACY" }
        show()

        chip("Experience").assert(hasText("1"))
        chip("Education").performClick()
        composeRule.onNodeWithText(legacy.title, substring = true).assertDoesNotExist()

        chip("Experience").performClick()
        composeRule.onNodeWithText(legacy.title, substring = true).assertIsDisplayed()
    }

    private fun chip(section: String) = composeRule.onNode(isSelectable() and hasText(section))

    private fun show() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ConfirmFactsScreen(
                    uiState = state,
                    actions = ConfirmFactsActions(
                        onBack = {},
                        onConfirm = {},
                        onEdit = { _, _ -> },
                        onAddOne = {},
                        onSkip = {},
                        onContinue = {},
                        onImportResume = {},
                    ),
                )
            }
        }
        composeRule.waitForIdle()
    }
}
