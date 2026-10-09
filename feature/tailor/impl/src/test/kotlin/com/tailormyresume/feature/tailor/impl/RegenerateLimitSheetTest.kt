package com.tailormyresume.feature.tailor.impl

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class RegenerateLimitSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tapAtTheLimitOpensTheNoRegenerationsLeftSheet() {
        show(ReviewFixtures.state(regenerationsUsed = 2))

        composeRule.onAllNodesWithText(
            "You've used both included regenerations. You can still edit any line by hand.",
        ).assertCountEquals(0)

        composeRule.onNodeWithContentDescription("No included regenerations left").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("No regenerations left").assertIsDisplayed()
        composeRule.onNodeWithText("0 of 2 left").assertIsDisplayed()
        composeRule.onNodeWithText(
            "You've used both included regenerations. You can still edit any line by hand.",
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Got it").assertIsDisplayed()

        composeRule.onNodeWithText("Got it").performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("No regenerations left").assertCountEquals(0)
    }

    @Test
    fun noSheetAppearsWhileRegenerationsRemain() {
        show(ReviewFixtures.state(regenerationsUsed = 0))

        composeRule.onNodeWithContentDescription("Regenerate", substring = true).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("No regenerations left").assertCountEquals(0)
    }

    @Test
    fun offlineAllReviewedShowsProgressLineNotBanner() {
        show(ReviewFixtures.allReviewed().copy(isOffline = true))

        composeRule.onNodeWithText("You're offline. Your reviews are saved on this phone.")
            .assertIsDisplayed()
        composeRule.onAllNodes(
            hasText("Every line in this resume traces to your facts.", substring = true),
        ).assertCountEquals(0)
        composeRule.onNodeWithText(" changes reviewed", substring = true).assertIsDisplayed()
    }

    private fun show(uiState: TailorUiState) {
        composeRule.setContent {
            TmrTheme {
                TailorScreen(
                    uiState = uiState,
                    actions = TailorActions(
                        onBack = {},
                        onPreviewExport = {},
                        onAccept = {},
                        onKeepOriginal = {},
                        onUndo = {},
                        onEditByHand = { _, _ -> },
                        onRegenerate = {},
                        onRetry = {},
                        onReportBullet = {},
                        onReportSection = {},
                        onEditFact = { _, _ -> },
                    ),
                )
            }
        }
        composeRule.waitForIdle()
    }
}
