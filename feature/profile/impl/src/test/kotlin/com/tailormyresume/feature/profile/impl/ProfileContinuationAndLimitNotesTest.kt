package com.tailormyresume.feature.profile.impl

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w360dp-h2400dp")
class ProfileContinuationAndLimitNotesTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun entry(id: String, title: String, bullets: List<String>) = ProfileEntry(
        id = id,
        category = EntryCategory.EXPERIENCE,
        title = title,
        organization = "Acme",
        startDate = "2024",
        endDate = "2025",
        bullets = bullets.mapIndexed { index, text -> EvidenceBullet("$id-b${index + 1}", text) },
        source = FactSource.IMPORTED,
        isConfirmed = true,
    )

    private fun show(vararg entries: ProfileEntry) {
        val profile = CandidateProfile("Priya", "", "", "", emptyList(), entries.toList())
        composeRule.setContent {
            TmrTheme {
                ProfileScreen(
                    uiState = ProfileUiState.Success(profile = profile),
                    actions = ProfileActions.None,
                    navigation = ProfileNavigation.None,
                )
            }
        }
        composeRule.onNode(hasContentDescription("Experience. ", substring = true) and hasClickAction()).performClick()
    }

    private fun lines(count: Int, longAt: Int? = null) =
        (1..count).map { if (it == longAt) "x".repeat(450) else "Did task $it." }

    @Test
    fun secondCardOfOneSplitRoleIsLabelledContinued() {
        show(entry("W-01", "Intern", lines(15)), entry("W-02", "Intern", lines(5)))

        composeRule.onNodeWithText("Continued").assertExists()
        composeRule.onNode(hasContentDescription("Continued. ", substring = true) and hasContentDescription("W-02", substring = true))
            .assertExists()
    }

    @Test
    fun differentRolesAreNotLabelledContinued() {
        show(entry("W-01", "Intern", lines(3)), entry("W-02", "Analyst", lines(3)))

        composeRule.onNodeWithText("Continued").assertDoesNotExist()
    }

    @Test
    fun entryThatIsTooLongAndOverFifteenShowsTheCombinedNoteAndSpeaksIt() {
        show(entry("W-01", "Intern", lines(20, longAt = 3)))

        composeRule.onNodeWithText("One line is too long and lines past 15", substring = true).assertExists()
        composeRule.onNode(hasContentDescription("lines past 15 are left out", substring = true)).assertExists()
    }

    @Test
    fun entryOverFifteenSpeaksTheOverFifteenNote() {
        show(entry("W-01", "Intern", lines(18)))

        composeRule.onNode(hasContentDescription("Lines after the first 15", substring = true)).assertExists()
    }
}
