package com.hirehop.feature.tailor.impl.credits

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.screenshot.HhTestDevices
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val GET_PACK = "Get an application pack"

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class CreditsGetPackTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(uiState: CreditsUiState, onGetPack: () -> Unit = {}) {
        composeRule.setContent {
            HhTheme {
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
    fun offline_hidesTheBuyRowSoNothingOpensWithoutAConnection() {
        show(CreditsUiState(stage = CreditsStage.READY, purchasedCredits = 4, isOffline = true))
        composeRule.onNodeWithText(GET_PACK).assertDoesNotExist()
    }
}
