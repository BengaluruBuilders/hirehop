package com.tailormyresume.feature.onboarding.impl.consent

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ConsentBackToStartTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var backToStartCount = 0
    private var readAgainCount = 0

    @Test
    fun declinedStateOffersBackToStart() {
        show(baseState(isDeclined = true))

        composeRule.onNodeWithText(BACK_TO_START).assertExists().assertIsDisplayed()

        composeRule.onNodeWithText(BACK_TO_START).performClick()

        assertEquals(1, backToStartCount)
        assertEquals(0, readAgainCount)
    }

    @Test
    fun declinedStateKeepsReviewAgain() {
        show(baseState(isDeclined = true))

        composeRule.onNodeWithText(REVIEW_AGAIN).performClick()

        assertEquals(1, readAgainCount)
    }

    @Test
    fun defaultStateHasNoBackToStart() {
        show(baseState())

        composeRule.onAllNodesWithText(BACK_TO_START).assertCountEquals(0)
    }

    private fun show(uiState: ConsentUiState) {
        composeRule.setContent {
            TmrTheme {
                ConsentScreen(
                    uiState = uiState,
                    actions = ConsentActions(
                        onPurposeToggle = { },
                        onAgree = {},
                        onNotNow = {},
                        onReadAgain = { readAgainCount++ },
                        onBack = {},
                        onBackToStart = { backToStartCount++ },
                    ),
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun baseState(
        acknowledged: Set<ConsentPurpose> = emptySet(),
        isSaving: Boolean = false,
        isReadOnly: Boolean = false,
        isDeclined: Boolean = false,
    ) = ConsentUiState(
        entries = consentPurposeStates().map { it.copy(isAcknowledged = it.purpose in acknowledged) },
        isSaving = isSaving,
        isReadOnly = isReadOnly,
        isDeclined = isDeclined,
    )

    private companion object {
        const val BACK_TO_START = "Back to start"
        const val REVIEW_AGAIN = "Review again"
    }
}
