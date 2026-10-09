package com.tailormyresume.feature.onboarding.impl.confirmfacts

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
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
class ConfirmFactsConfirmedTooLongTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val confirmedWithLongBullet = ProfileEntry(
        id = "LONG",
        category = EntryCategory.EXPERIENCE,
        title = "Data intern",
        organization = "Nashik",
        startDate = "May 2025",
        endDate = "Jul 2025",
        bullets = listOf(EvidenceBullet("LONG-b1", "S" + "a".repeat(448) + ".")),
        source = FactSource.IMPORTED,
        isConfirmed = true,
    )

    @Test
    fun confirmedEntryWithATooLongBulletStillShowsTheStatusAndTheEditFirstNote() {
        val scenario = DebugScenario.FULLY_CONFIRMED
        val state = ConfirmFactsScenarioMapper.withProfile(
            state = ConfirmFactsScenarioMapper.seed(scenario),
            profile = sampleProfile.copy(entries = listOf(confirmedWithLongBullet)),
            scenario = scenario,
        )
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

        composeRule.onNodeWithText("Too long").assertExists()
        composeRule.onNodeWithText("A line here is over 400 characters", substring = true).assertExists()
    }
}
