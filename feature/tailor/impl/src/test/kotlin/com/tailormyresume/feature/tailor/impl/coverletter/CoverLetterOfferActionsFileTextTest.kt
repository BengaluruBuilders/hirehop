package com.tailormyresume.feature.tailor.impl.coverletter

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
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
class CoverLetterOfferActionsFileTextTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun offer_showsNoTextWithTheExportedFileName() {
        composeRule.setContent {
            TmrTheme {
                CoverLetterScreen(
                    uiState = CoverLetterUiState(
                        stage = CoverLetterStage.OFFER,
                        jobCompany = "Northwind GCC",
                        exportedFileName = "Priya_Northwind.pdf",
                    ),
                    actions = CoverLetterActions(
                        onWriteOne = {},
                        onBeginEdit = {},
                        onEditTextChanged = {},
                        onSaveEdit = {},
                        onCancelEdit = {},
                        onReportInaccurate = {},
                        onDismissMessage = {},
                        onRetry = {},
                        onNavigateBack = {},
                        onSkipLetter = {},
                        onPreviewExport = {},
                        onPrepQuestions = {},
                    ),
                )
            }
        }

        composeRule.onAllNodes(hasText("Priya_Northwind", substring = true)).assertCountEquals(0)
    }
}
