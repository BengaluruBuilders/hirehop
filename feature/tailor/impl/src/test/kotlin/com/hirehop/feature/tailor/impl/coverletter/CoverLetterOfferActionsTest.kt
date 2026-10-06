package com.hirehop.feature.tailor.impl.coverletter

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class CoverLetterOfferActionsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var prepClicks = 0
    private var writeClicks = 0
    private var skipClicks = 0

    private fun show(uiState: CoverLetterUiState) {
        composeRule.setContent {
            HhTheme {
                CoverLetterScreen(
                    uiState = uiState,
                    actions = CoverLetterActions(
                        onWriteOne = { writeClicks++ },
                        onBeginEdit = {},
                        onEditTextChanged = {},
                        onSaveEdit = {},
                        onCancelEdit = {},
                        onReportInaccurate = {},
                        onDismissMessage = {},
                        onRetry = {},
                        onNavigateBack = {},
                        onSkipLetter = { skipClicks++ },
                        onPreviewExport = {},
                        onPrepQuestions = { prepClicks++ },
                    ),
                )
            }
        }
    }

    @Test
    fun prepForTheInterview_callsOnPrepQuestions() {
        show(CoverLetterUiState(stage = CoverLetterStage.OFFER, jobCompany = "Northwind GCC"))

        composeRule.onNodeWithText("Prep for the interview").performClick()

        assertThat(prepClicks).isEqualTo(1)
    }

    @Test
    fun offerButtons_sitInTheCardAndCallTheirActions() {
        show(CoverLetterUiState(stage = CoverLetterStage.OFFER, jobCompany = "Northwind GCC"))

        composeRule.onNodeWithText("Not now").performClick()
        composeRule.onNodeWithText("Write one").performClick()

        assertThat(skipClicks).isEqualTo(1)
        assertThat(writeClicks).isEqualTo(1)
    }

    @Test
    fun fileRow_showsTheExportedFileName() {
        show(
            CoverLetterUiState(
                stage = CoverLetterStage.OFFER,
                jobCompany = "Northwind GCC",
                exportedFileName = "Priya_Northwind.pdf",
            ),
        )

        composeRule.onNodeWithContentDescription("Priya_Northwind.pdf").assertExists()
    }

    @Test
    fun fileRow_isAbsentWithoutAnExport() {
        show(CoverLetterUiState(stage = CoverLetterStage.OFFER, jobCompany = "Northwind GCC"))

        composeRule.onAllNodes(hasText(".pdf", substring = true)).assertCountEquals(0)
    }
}
