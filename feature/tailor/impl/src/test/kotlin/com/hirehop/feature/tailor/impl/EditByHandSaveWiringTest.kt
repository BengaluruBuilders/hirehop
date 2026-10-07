package com.hirehop.feature.tailor.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.screenshot.HhTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class EditByHandSaveWiringTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val calls = mutableListOf<Pair<String, String>>()

    @Test
    fun blankSaveDoesNotCallOnEditByHandAndKeepsTheSheetOpen() {
        setContent()

        composeRule.onNodeWithText("Edit by hand").performClick()
        composeRule.waitForIdle()
        clearEditField()

        composeRule.onNodeWithText("Save line").performClick()
        composeRule.waitForIdle()

        assertThat(calls).isEmpty()
        composeRule.onNodeWithText("Add this to save.").assertIsDisplayed()
        composeRule.onNodeWithText("Save line").assertIsDisplayed()
    }

    @Test
    fun typingThenSavingCallsOnEditByHandOnce() {
        setContent()

        composeRule.onNodeWithText("Edit by hand").performClick()
        composeRule.waitForIdle()
        clearEditField()

        composeRule.onNodeWithText("Save line").performClick()
        composeRule.waitForIdle()
        assertThat(calls).isEmpty()

        composeRule.onNode(hasSetTextAction()).performTextInput("Cut p95 latency 40%")
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Save line").performClick()
        composeRule.waitForIdle()

        assertThat(calls).containsExactly(ReviewFixtures.WEEKLY to "Cut p95 latency 40%")
        assertThat(composeRule.onAllNodesWithText("Save line").fetchSemanticsNodes()).isEmpty()
    }

    private fun setContent() {
        composeRule.setContent {
            HhTheme(darkTheme = false) {
                ReviewOverlays(
                    state = ReviewFixtures.state(),
                    actions = recordingActions(),
                    interaction = ReviewInteraction(initialBulletId = ReviewFixtures.WEEKLY),
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun clearEditField() {
        composeRule.onNode(hasSetTextAction()).performTextClearance()
        composeRule.waitForIdle()
    }

    private fun recordingActions(): TailorActions = TailorActions(
        onBack = {},
        onPreviewExport = {},
        onAccept = {},
        onKeepOriginal = {},
        onUndo = {},
        onEditByHand = { bulletId, text -> calls += bulletId to text },
        onRegenerate = {},
        onRetry = {},
        onReportBullet = {},
        onReportSection = {},
        onEditFact = { _, _ -> },
    )
}
