package com.tailormyresume.feature.profile.impl.guidedform

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w360dp-h2400dp")
class GuidedFormBatchTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(state: GuidedFormUiState) = composeRule.setContent {
        TmrTheme { GuidedFormScreen(uiState = state, actions = GuidedFormActions.None, onBack = {}) }
    }

    private fun filed(id: String, title: String) = ProfileEntry(
        id = id,
        category = EntryCategory.EDUCATION,
        title = title,
        organization = "",
        startDate = "",
        endDate = "",
        bullets = emptyList(),
        source = FactSource.USER_STATED,
        isConfirmed = true,
    )

    @Test
    fun savedForLaterShowsThreeOfFourWithThreeFilledBars() {
        val done = setOf(GuidedStep.CONTACT, GuidedStep.EDUCATION, GuidedStep.SKILLS)
        show(GuidedFormUiState(saved = GuidedSaved(completedSteps = 3, totalSteps = 4, entryIds = listOf("U-01"), doneSteps = done)))

        composeRule.onNodeWithText("3 of 4").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Skills, done").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Contact, done").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Education, done").assertIsDisplayed()
        assertThat(composeRule.onAllNodesWithContentDescription("Experience, done").fetchSemanticsNodes()).isEmpty()
        assertThat(composeRule.onAllNodesWithText("4 of 4").fetchSemanticsNodes()).isEmpty()
    }

    @Test
    fun labelRowMarksFinishedStepsWithACheck() {
        show(GuidedFormUiState(stepIndex = 1, completedSteps = setOf(GuidedStep.CONTACT)))

        composeRule.onNodeWithContentDescription("Contact, done").assertIsDisplayed()
        assertThat(composeRule.onAllNodesWithContentDescription("Education, done").fetchSemanticsNodes()).isEmpty()
    }

    @Test
    fun experienceStepOffersYesAndNoRows() {
        show(GuidedFormUiState(stepIndex = 3))

        composeRule.onNodeWithText("Yes").assertIsDisplayed()
        composeRule.onNodeWithText("No, not yet").assertIsDisplayed()
        composeRule.onNodeWithText("Continue to projects").assertIsDisplayed()
        assertThat(composeRule.onAllNodesWithText("No work experience yet?", substring = true).fetchSemanticsNodes()).isEmpty()
    }

    @Test
    fun noNotYetSelectsTheRowAndShowsTheReassurance() {
        show(GuidedFormUiState(stepIndex = 3, experienceChoice = ExperienceChoice.NO))

        composeRule.onNodeWithText("No, not yet").assertIsSelected()
        composeRule.onNodeWithText("No work experience yet?", substring = true).assertIsDisplayed()
    }

    @Test
    fun yesKeepsContinueToProjectsAsThePrimaryAndOpensTheJobEditor() {
        var jobEditorOpened = false
        var moved = false
        composeRule.setContent {
            TmrTheme {
                GuidedFormScreen(
                    uiState = GuidedFormUiState(stepIndex = 3, experienceChoice = ExperienceChoice.YES),
                    actions = GuidedFormActions.None.copy(onAddJob = { jobEditorOpened = true }, onNext = { moved = true }),
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Yes").assertIsSelected()
        composeRule.onNodeWithText("Yes").performClick()
        assertThat(jobEditorOpened).isTrue()
        composeRule.onNodeWithText("Continue to projects").performClick()
        assertThat(moved).isTrue()
        assertThat(composeRule.onAllNodesWithText("Add a job or internship").fetchSemanticsNodes()).isEmpty()
    }

    @Test
    fun skillsNoteSaysEachSkillBecomesAUserStatedFact() {
        show(GuidedFormUiState(stepIndex = 2))

        composeRule.onNodeWithText("Each skill becomes a user-stated fact.", substring = true).assertIsDisplayed()
    }

    @Test
    fun filedFactsOfTheDoneStepOfferEdit() {
        show(
            GuidedFormUiState(
                stepIndex = 2,
                filedEntries = listOf(filed("U-01", "B.Tech Computer Science"), filed("U-02", "DBMS")),
            ),
        )

        assertThat(composeRule.onAllNodesWithText("Edit").fetchSemanticsNodes()).hasSize(2)
    }
}
