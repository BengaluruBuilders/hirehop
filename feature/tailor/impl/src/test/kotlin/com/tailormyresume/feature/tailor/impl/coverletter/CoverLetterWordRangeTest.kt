package com.tailormyresume.feature.tailor.impl.coverletter

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
class CoverLetterWordRangeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(wordCount: Int) {
        val paragraph = CoverLetterParagraph(
            ordinal = 0,
            text = List(wordCount) { "word" }.joinToString(" "),
            sentences = emptyList(),
            facts = emptyList(),
            basis = CoverLetterBasis.PLAIN,
        )
        composeRule.setContent {
            TmrTheme {
                CoverLetterScreen(
                    uiState = CoverLetterUiState(
                        stage = CoverLetterStage.READY,
                        jobCompany = "Northwind GCC",
                        paragraphs = listOf(paragraph),
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
    }

    @Test
    fun countInsideTheRange_showsNoRangeNote() {
        show(wordCount = 180)

        composeRule.onNodeWithText("180 words · 1 paragraph").assertExists()
        composeRule.onAllNodesWithText("the range is 150 to 220", substring = true).assertCountEquals(0)
    }

    @Test
    fun countOutsideTheRange_saysTheRange() {
        show(wordCount = 85)

        composeRule.onNodeWithText("85 words · the range is 150 to 220").assertExists()
    }
}
