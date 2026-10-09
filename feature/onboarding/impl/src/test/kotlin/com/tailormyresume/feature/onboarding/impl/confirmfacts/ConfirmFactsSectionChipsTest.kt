package com.tailormyresume.feature.onboarding.impl.confirmfacts

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
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
import com.tailormyresume.core.testing.data.sampleProfile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class ConfirmFactsSectionChipsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val state = ConfirmFactsScenarioMapper.withProfile(
        state = ConfirmFactsScenarioMapper.seed(DebugScenario.PARTLY_CONFIRMED),
        profile = sampleProfile,
        scenario = DebugScenario.PARTLY_CONFIRMED,
    )

    @Test
    fun chipsNameEachSectionThatListsFactsWithItsCount() {
        show()

        chip("Education").assertIsDisplayed()
        chip("Education").assert(hasText("1"))
        chip("Projects").assertIsDisplayed()
        chip("Projects").assert(hasText("1"))
    }

    @Test
    fun skillsAreNotConfirmableSoTheyGetNoChip() {
        show()

        composeRule.onAllNodes(isSelectable() and hasText("Skills")).assertCountEquals(0)
    }

    @Test
    fun tappingAChipShowsOnlyThatSectionsFacts() {
        show()
        val project = state.facts.first { it.section == ConfirmFactsSection.Projects }
        val education = state.facts.first { it.section == ConfirmFactsSection.Education }

        chip("Projects").performClick()

        composeRule.onNodeWithText(project.title, substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(education.title, substring = true).assertDoesNotExist()
    }

    @Test
    fun theFactDetailIsItsOwnLine() {
        show()
        val fact = state.facts.first { it.section == ConfirmFactsSection.Projects }
        assertThat(fact.detail).isNotEmpty()

        composeRule.onNodeWithText(fact.title).assertIsDisplayed()
        composeRule.onNodeWithText(fact.detail).assertIsDisplayed()
    }

    @Test
    fun aFlaggedConfirmedFactIsNotCountedAsConfirmedAndFollowsTheChip() {
        val flagged = state.facts.first { it.section == ConfirmFactsSection.Education }
            .copy(isConfirmed = true, hasTooLongBullet = true)
        val flaggedState = state.copy(
            sections = state.sections.map { entry ->
                entry.copy(facts = entry.facts.map { if (it.id == flagged.id) flagged else it })
            },
        )
        assertThat(flaggedState.facts.count { it.isConfirmedWithinLimits }).isEqualTo(0)

        show(flaggedState)

        chip("Education").assert(hasText("1"))
        composeRule.onNodeWithText("1 fact confirmed").assertDoesNotExist()
        composeRule.onNodeWithText(flagged.title, substring = true).assertIsDisplayed()

        chip("Projects").performClick()
        composeRule.onNodeWithText(flagged.title, substring = true).assertDoesNotExist()

        chip("Education").performClick()
        composeRule.onNodeWithText(flagged.title, substring = true).assertIsDisplayed()
    }

    private fun chip(section: String) = composeRule.onNode(isSelectable() and hasText(section))

    private fun show(uiState: ConfirmFactsUiState = state) {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ConfirmFactsScreen(
                    uiState = uiState,
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
