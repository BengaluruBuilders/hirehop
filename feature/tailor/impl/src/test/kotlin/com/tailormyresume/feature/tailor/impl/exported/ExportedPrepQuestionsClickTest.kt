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
class ExportedPrepQuestionsClickTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var prepClicks = 0
    private var letterClicks = 0

    @Test
    fun tappingGetPrepQuestionsCallsItsAction() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ExportedScreen(
                    uiState = ExportedUiState(
                        stage = ExportedStage.READY,
                        format = ExportFormat.PDF,
                        fileName = "Priya_Northwind.pdf",
                        fileOnDevice = true,
                        creditsKnown = true,
                        creditsLeft = 4,
                    ),
                    actions = ExportedActions(
                        onOpenStatusSheet = {},
                        onDismissStatusSheet = {},
                        onConfirmStatus = {},
                        onUndoStatus = {},
                        onDismissUndo = {},
                        onShare = {},
                        onOpen = {},
                        onGetPrepQuestions = { prepClicks++ },
                        onWriteCoverLetter = { letterClicks++ },
                        onDone = {},
                        onNavigateBack = {},
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Get prep questions").performClick()

        assertThat(prepClicks).isEqualTo(1)
        assertThat(letterClicks).isEqualTo(0)
    }
}
