package com.tailormyresume.feature.tailor.impl.exportpreview

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
class ExportPreviewCancelHiddenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(uiState: ExportPreviewUiState) {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ExportPreviewScreen(
                    uiState = uiState,
                    actions = ExportPreviewActions(
                        onSelectFormat = {},
                        onExport = {},
                        onRetry = {},
                        onNavigateBack = {},
                        onBuyCredits = {},
                    ),
                )
            }
        }
    }

    @Test
    fun cancelIsShownWhileTheFileIsBeingMade() {
        show(ExportPreviewUiState(stage = ExportPreviewStage.EXPORTING, isSpending = false))

        composeRule.onNodeWithText("Cancel").assertExists()
    }

    @Test
    fun cancelIsHiddenWhileTheCreditIsBeingSpent() {
        show(ExportPreviewUiState(stage = ExportPreviewStage.EXPORTING, isSpending = true))

        composeRule.onNodeWithText("Cancel").assertDoesNotExist()
    }
}
