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
class ProfileTooLongBulletTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun entry(id: String, isConfirmed: Boolean) = ProfileEntry(
        id = id,
        category = EntryCategory.EXPERIENCE,
        title = "Intern at Acme",
        organization = "",
        startDate = "",
        endDate = "",
        bullets = listOf(EvidenceBullet("$id-b1", "S" + "a".repeat(448) + ".")),
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
    fun confirmedEntryShowsTooLongAndTheNote() {
        show(isConfirmed = true)

        composeRule.onNodeWithText("Too long").assertExists()
        composeRule.onNodeWithText("A line here is over 400 characters", substring = true).assertExists()
    }

    @Test
    fun tooLongNoteIsPartOfTheCardDescription() {
        show(isConfirmed = true)

        composeRule.onNode(hasContentDescription("Edit it to use it.", substring = true)).assertExists()
    }

    @Test
    fun unconfirmedEntryShowsTooLongTheNoteAndNoConfirmButton() {
        show(isConfirmed = false)

        composeRule.onNodeWithText("Too long").assertExists()
        composeRule.onNodeWithText("A line here is over 400 characters", substring = true).assertExists()
        composeRule.onNodeWithText("Confirm").assertDoesNotExist()
    }
}
