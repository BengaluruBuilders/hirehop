package com.tailormyresume.feature.tailor.impl.exported

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val SEPARATOR = " · "

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ExportedFileDetailTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun detailShowsFormatPagesAndSize() {
        setContent(
            readyState(creditsLeft = 4, source = ExportedCreditSource.PAID).copy(
                format = ExportFormat.PDF,
                pageCount = 1,
                templateName = "Plain",
                fileSizeBytes = 49152L,
            ),
        )
        composeRule.onNodeWithText("PDF${SEPARATOR}1 page${SEPARATOR}48 KB").assertIsDisplayed()
        composeRule.onAllNodesWithText("Plain", substring = true).assertCountEquals(0)
    }

    @Test
    fun detailForDocxHasNoPageCount() {
        setContent(
            readyState(creditsLeft = 4, source = ExportedCreditSource.PAID).copy(
                format = ExportFormat.DOCX,
                pageCount = null,
                templateName = "Plain",
                fileSizeBytes = 20480L,
            ),
        )
        composeRule.onNodeWithText("DOCX${SEPARATOR}20 KB").assertIsDisplayed()
    }

    @Test
    fun detailWithoutKnownSizeShowsFormatAndPages() {
        setContent(
            readyState(creditsLeft = 4, source = ExportedCreditSource.PAID).copy(
                format = ExportFormat.PDF,
                pageCount = 2,
                fileSizeBytes = null,
            ),
        )
        composeRule.onNodeWithText("PDF${SEPARATOR}2 pages").assertIsDisplayed()
    }

    private fun setContent(uiState: ExportedUiState) {
        composeRule.setContent { ExportedDetailHost(uiState) }
    }
}

private const val JOB_TITLE = "Associate Analyst"

private const val COMPANY = "Northwind GCC"

private fun readyState(creditsLeft: Int, source: ExportedCreditSource): ExportedUiState = ExportedUiState(
    stage = ExportedStage.READY,
    format = ExportFormat.PDF,
    jobTitle = JOB_TITLE,
    jobCompany = COMPANY,
    fileName = "Priya-Deshmukh_Northwind-GCC_Associate-Analyst.pdf",
    fileOnDevice = true,
    pageCount = 1,
    templateName = "Plain",
    creditsKnown = true,
    creditSource = source,
    creditsLeft = creditsLeft,
    creditsNeverExpire = true,
)

@Composable
private fun ExportedDetailHost(uiState: ExportedUiState) {
    TmrTheme(darkTheme = false) {
        ExportedScreen(
            uiState = uiState,
            actions = ExportedActions(
                onOpenStatusSheet = {},
                onDismissStatusSheet = {},
                onConfirmStatus = {},
                onUndoStatus = {},
                onDismissUndo = {},
                onShare = {},
                onOpen = {},
                onGetPrepQuestions = {},
                onWriteCoverLetter = {},
                onDone = {},
                onNavigateBack = {},
            ),
        )
    }
}
