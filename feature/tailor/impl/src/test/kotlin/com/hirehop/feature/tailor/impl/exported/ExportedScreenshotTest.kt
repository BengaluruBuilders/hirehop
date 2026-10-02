package com.hirehop.feature.tailor.impl.exported

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.ExportFormat
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TRACKED_OUTPUT_DIR = "src/test/screenshots"

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class ExportedScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun paidCreditUsed_rollsTheCounterDownByOne() {
        capture("ExportedPaidCreditUsed", readyState(creditsLeft = 4, source = ExportedCreditSource.PAID))
    }

    @Test
    fun freeCreditUsed_saysNothingWasCharged() {
        capture("ExportedFreeCreditUsed", readyState(creditsLeft = 0, source = ExportedCreditSource.FREE))
    }

    @Test
    fun markedApplied_swapsThePromptForTheStatusChipAndOffersUndo() {
        capture(
            "ExportedMarkedApplied",
            readyState(creditsLeft = 4, source = ExportedCreditSource.PAID).copy(
                status = ApplicationStatus.APPLIED,
                markedOn = "14 Apr 2027",
                undoStatus = ApplicationStatus.SAVED,
            ),
        )
    }

    @Test
    fun blankRoleAndCompany_useTheFallbackWordsAndNoDanglingSeparator() {
        capture(
            "ExportedBlankJob",
            readyState(creditsLeft = 4, source = ExportedCreditSource.PAID).copy(jobTitle = "", jobCompany = ""),
        )
    }

    @Test
    fun blankCompany_usesTheFallbackWordForTheCompanyOnly() {
        capture(
            "ExportedBlankCompany",
            readyState(creditsLeft = 4, source = ExportedCreditSource.PAID).copy(jobCompany = ""),
        )
    }

    @Test
    fun fileGone_disablesShareAndOpenAndSaysWhy() {
        capture(
            "ExportedFileGone",
            readyState(creditsLeft = 4, source = ExportedCreditSource.PAID).copy(fileOnDevice = false),
        )
    }

    @Test
    fun noFile_offersTheWayBack() {
        capture("ExportedNoFile", ExportedUiState(stage = ExportedStage.NO_FILE, jobTitle = JOB_TITLE, jobCompany = COMPANY))
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun paidCreditUsed_atLargeTextKeepsEveryPartWhole() {
        capture(
            "ExportedPaidCreditUsedFont200",
            readyState(creditsLeft = 4, source = ExportedCreditSource.PAID),
            device = HhTestDevices.boardLargeFont,
        )
    }

    private fun capture(
        screenName: String,
        uiState: ExportedUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent { ExportedHost(uiState = uiState, dark = darkTheme.value) }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
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
private fun ExportedHost(
    uiState: ExportedUiState,
    dark: Boolean,
) {
    HhTheme(darkTheme = dark) {
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
