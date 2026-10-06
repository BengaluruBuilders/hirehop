package com.hirehop.feature.tailor.impl.coverletter

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.screenshot.HhTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
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
            HhTheme {
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
    fun countInsideTheRange_saysInTheRange() {
        show(wordCount = 180)

        composeRule.onNodeWithText("180 words · in the 150 to 220 range").assertExists()
    }

    @Test
    fun countOutsideTheRange_doesNotClaimTheRange() {
        show(wordCount = 85)

        composeRule.onNodeWithText("85 words · the range is 150 to 220").assertExists()
        composeRule.onAllNodesWithText("in the 150 to 220 range", substring = true).assertCountEquals(0)
    }

    @Test
    fun initials_useTheFirstLetterOfEachWord() {
        assertThat("Northwind GCC".initials()).isEqualTo("NG")
        assertThat("  kestrel  ".initials()).isEqualTo("K")
        assertThat("".initials()).isEqualTo("")
    }
}
