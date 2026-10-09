package com.tailormyresume.feature.profile.impl

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
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
class ProfileTooManyBulletsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun entry(id: String, isConfirmed: Boolean) = ProfileEntry(
        id = id,
        category = EntryCategory.EXPERIENCE,
        title = "Intern at Acme",
        organization = "",
        startDate = "",
        endDate = "",
        bullets = (1..18).map { EvidenceBullet("$id-b$it", "Did task $it.") },
        source = FactSource.IMPORTED,
        isConfirmed = isConfirmed,
    )

    private fun show(isConfirmed: Boolean) {
        val profile = CandidateProfile("Priya", "", "", "", emptyList(), listOf(entry("W-01", isConfirmed)))
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

    @Test
    fun confirmedEntryShowsTooManyLinesAndTheNote() {
        show(isConfirmed = true)

        composeRule.onNodeWithText("Too many lines").assertExists()
        composeRule.onNodeWithText("Lines after the first 15 are left out", substring = true).assertExists()
    }

    @Test
    fun unconfirmedEntryShowsTheNoteAndNoConfirmButton() {
        show(isConfirmed = false)

        composeRule.onNodeWithText("Too many lines").assertExists()
        composeRule.onNodeWithText("Confirm").assertDoesNotExist()
    }

    @Test
    fun confirmEntryLeavesAnEntryWithTooManyLinesOpen() {
        val profile = CandidateProfile("P", "", "", "", emptyList(), listOf(entry("W-01", isConfirmed = false)))

        assertThat(profile.confirmEntry("W-01").entries.single().isConfirmed).isFalse()
    }
}
