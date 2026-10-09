package com.tailormyresume.feature.tailor.impl.exported

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ExportedOpenUnavailableScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var shares = 0
    private var dismissals = 0

    private fun show(uiState: ExportedUiState) {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ExportedScreen(
                    uiState = uiState,
                    actions = ExportedActions(
                        onOpenStatusSheet = {},
                        onDismissStatusSheet = {},
                        onConfirmStatus = {},
                        onUndoStatus = {},
                        onDismissUndo = {},
                        onShare = { shares++ },
                        onOpen = {},
                        onGetPrepQuestions = {},
                        onWriteCoverLetter = {},
                        onDone = {},
                        onNavigateBack = {},
                        onDismissOpenUnavailable = { dismissals++ },
                    ),
                )
            }
        }
    }

    private fun readyState() = ExportedUiState(
        stage = ExportedStage.READY,
        format = ExportFormat.DOCX,
        fileName = "Priya_Northwind.docx",
        fileOnDevice = true,
        creditsKnown = true,
        creditsLeft = 4,
    )

    @Test
    fun whenNoAppCanOpenTheFile_theMessageOffersShareInstead() {
        show(readyState().copy(openUnavailable = true))
        composeRule.waitForIdle()

        composeRule.onNodeWithText("No app on this phone can open this file.", substring = true).assertExists()
        composeRule.onNodeWithText("Share instead").performClick()

        assertThat(shares).isEqualTo(1)
        assertThat(dismissals).isEqualTo(1)
    }
}
