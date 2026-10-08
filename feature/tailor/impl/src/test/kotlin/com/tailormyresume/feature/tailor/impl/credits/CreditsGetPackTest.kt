package com.tailormyresume.feature.tailor.impl.credits

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val GET_PACK = "Get an application pack"

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class CreditsGetPackTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(uiState: CreditsUiState, onGetPack: () -> Unit = {}) {
        composeRule.setContent {
            TmrTheme {
                CreditsScreen(
                    uiState = uiState,
                    actions = CreditsActions(
                        onGetPack = onGetPack,
                        onAskRefund = {},
                        onContactHelp = {},
                        onRetry = {},
                        onNavigateBack = {},
                    ),
                )
            }
        }
    }

    @Test
    fun online_opensThePackWhenTheRowIsTapped() {
        var opened = 0
        show(CreditsUiState(stage = CreditsStage.READY, purchasedCredits = 4), onGetPack = { opened++ })
        composeRule.onNodeWithText(GET_PACK).assertIsEnabled().performClick()
        assertEquals(1, opened)
    }

    @Test
    fun offline_keepsTheBuyRowVisibleButDisabledAndNothingOpens() {
        var opened = 0
        show(
            CreditsUiState(stage = CreditsStage.READY, purchasedCredits = 4, isOffline = true),
            onGetPack = { opened++ },
        )
        composeRule.onNodeWithText(GET_PACK).assertIsNotEnabled().performClick()
        assertEquals(0, opened)
    }
}
