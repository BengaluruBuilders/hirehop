package com.tailormyresume.feature.profile.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w360dp-h2400dp")
class ProfileSectionExpandTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val profile = CandidateProfile(
        fullName = "Priya Deshmukh",
        email = "priya.d@example.com",
        phone = "",
        headline = "Data Operations Associate",
        skills = listOf("SQL"),
        entries = listOf(
            entry("U-01", EntryCategory.EDUCATION, "B.Tech Computer Science"),
            entry("C-02", EntryCategory.PROJECT, "Placement Stats Dashboard"),
        ),
    )

    private fun entry(id: String, category: EntryCategory, title: String) = ProfileEntry(
        id = id,
        category = category,
        title = title,
        organization = "",
        startDate = "",
        endDate = "",
        bullets = emptyList(),
        source = FactSource.IMPORTED,
        isConfirmed = true,
    )

    private fun sectionRow(name: String) = hasContentDescription("$name. ", substring = true) and hasClickAction()

    private fun show() = composeRule.setContent {
        TmrTheme {
            ProfileScreen(
                uiState = ProfileUiState.Success(profile = profile),
                actions = ProfileActions.None,
                navigation = ProfileNavigation.None,
            )
        }
    }

    @Test
    fun tappingASectionRowShowsItsFactsAndKeepsTheOtherRows() {
        show()
        composeRule.onNodeWithText("Placement Stats Dashboard", substring = true).assertDoesNotExist()

        composeRule.onNode(sectionRow("Projects")).performClick()

        composeRule.onNodeWithText("Placement Stats Dashboard", substring = true).assertIsDisplayed()
        composeRule.onNode(sectionRow("Education")).assertIsDisplayed()
    }

    @Test
    fun tappingTheOpenRowAgainCollapsesIt() {
        show()
        composeRule.onNode(sectionRow("Projects")).performClick()

        composeRule.onNode(sectionRow("Projects")).performClick()

        composeRule.onNodeWithText("Placement Stats Dashboard", substring = true).assertDoesNotExist()
    }
}
