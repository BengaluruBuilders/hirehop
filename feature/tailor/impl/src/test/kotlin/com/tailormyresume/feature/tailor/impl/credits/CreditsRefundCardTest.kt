package com.tailormyresume.feature.tailor.impl.credits

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
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
class CreditsRefundCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun noPurchasesShowsNoGooglePlayClaimAndNoRefundButton() {
        composeRule.setContent {
            Host(CreditsUiState(stage = CreditsStage.READY, freeCredits = 1))
        }

        assertThat(
            composeRule.onAllNodesWithText(GOOGLE_PLAY_CLAIM, substring = true).fetchSemanticsNodes(),
        ).isEmpty()
        assertThat(
            composeRule.onAllNodesWithText(REFUND_BUTTON).fetchSemanticsNodes(),
        ).isEmpty()
        composeRule.onNodeWithText(HELP_TITLE).assertExists()
    }

    @Test
    fun purchasesKeepGooglePlayCopyAndButton() {
        composeRule.setContent {
            Host(
                CreditsUiState(
                    stage = CreditsStage.READY,
                    purchasedCredits = 4,
                    purchases = listOf(
                        CreditsPurchaseEntry(
                            orderId = "GPA.1",
                            credits = 5,
                            formattedPrice = "149",
                            formattedDate = "14 Apr 2027",
                            isPending = false,
                        ),
                    ),
                ),
            )
        }

        assertThat(
            composeRule.onAllNodesWithText(GOOGLE_PLAY_CLAIM, substring = true).fetchSemanticsNodes(),
        ).isNotEmpty()
        assertThat(
            composeRule.onAllNodesWithText(REFUND_BUTTON).fetchSemanticsNodes(),
        ).isNotEmpty()
    }

    @Test
    fun historyWithoutListedPurchasesKeepsGooglePlayCopyAndButton() {
        composeRule.setContent {
            Host(CreditsUiState(stage = CreditsStage.READY, purchases = emptyList(), hasPurchaseHistory = true))
        }

        assertThat(
            composeRule.onAllNodesWithText(GOOGLE_PLAY_CLAIM, substring = true).fetchSemanticsNodes(),
        ).isNotEmpty()
        assertThat(
            composeRule.onAllNodesWithText(REFUND_BUTTON).fetchSemanticsNodes(),
        ).isNotEmpty()
        assertThat(
            composeRule.onAllNodesWithText(NOT_BOUGHT_COPY, substring = true).fetchSemanticsNodes(),
        ).isEmpty()
    }
}

private const val GOOGLE_PLAY_CLAIM = "You paid through Google Play"
private const val REFUND_BUTTON = "Ask for a refund"
private const val NOT_BOUGHT_COPY = "You have not bought credits yet"
private const val HELP_TITLE = "How refunds work"

@androidx.compose.runtime.Composable
private fun Host(uiState: CreditsUiState) {
    TmrTheme {
        CreditsScreen(
            uiState = uiState,
            actions = CreditsActions(
                onGetPack = {},
                onAskRefund = {},
                onContactHelp = {},
                onRetry = {},
                onNavigateBack = {},
            ),
        )
    }
}
