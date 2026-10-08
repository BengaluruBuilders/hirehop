package com.tailormyresume.feature.tailor.impl.exportpreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.screenshot.TmrTestDevice
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TRACKED_OUTPUT_DIR = "src/test/screenshots"

private const val PDF_NAME = "Priya-Deshmukh_Northwind-GCC_Associate-Analyst.pdf"

private val SAMPLE_SHEET = ExportPreviewSheet(
    name = "Priya Deshmukh",
    contactLine = "Pune, Maharashtra · priya.d@example.com · +91 98XXX XXXXX",
    headline = "",
    skillsHeading = "Skills",
    skills = listOf("SQL", "Advanced Excel (pivots)", "Power BI"),
    sections = listOf(
        ExportPreviewSection(
            heading = "Experience",
            entries = listOf(
                ExportPreviewEntry(
                    title = "Data Operations Associate",
                    organization = "Saffron Retail, Pune",
                    dateRange = "Jul 2025 to now",
                    bullets = listOf(
                        "Built weekly sales reports in Excel for 40 stores.",
                        "Cleaned order data with SQL for weekly reporting.",
                    ),
                ),
                ExportPreviewEntry(
                    title = "Data intern",
                    organization = "Kiran Agro Exports, Nashik",
                    dateRange = "May to Jul 2025",
                    bullets = listOf("Cleaned 12,000 rows of sales data in Excel and built weekly pivot reports."),
                ),
            ),
        ),
        ExportPreviewSection(
            heading = "Projects",
            entries = listOf(
                ExportPreviewEntry(
                    title = "Placement Stats Dashboard",
                    organization = "Power BI",
                    dateRange = "",
                    bullets = listOf("Built a Power BI dashboard of placement data across 3 batches for the T&P cell."),
                ),
            ),
        ),
        ExportPreviewSection(
            heading = "Education",
            entries = listOf(
                ExportPreviewEntry(
                    title = "B.Tech Computer Science",
                    organization = "",
                    dateRange = "2024",
                    bullets = listOf("Coursework: DBMS (SQL)"),
                ),
            ),
        ),
        ExportPreviewSection(
            heading = "Achievements",
            entries = listOf(
                ExportPreviewEntry(
                    title = "Smart India Hackathon 2024, internal-round finalist",
                    organization = "",
                    dateRange = "",
                    bullets = emptyList(),
                ),
            ),
        ),
    ),
)

private fun readyState(): ExportPreviewUiState = ExportPreviewUiState(
    stage = ExportPreviewStage.PREVIEW_READY,
    jobTitle = "Associate Analyst",
    jobCompany = "Northwind GCC",
    sheet = SAMPLE_SHEET,
    fileName = PDF_NAME,
    creditsKnown = true,
    freeCredits = 1,
)

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ExportPreviewScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun rendering_listsTheStepsOfSettingThePage() {
        capture(
            "ExportPreviewRendering",
            ExportPreviewUiState(jobTitle = "Associate Analyst", jobCompany = "Northwind GCC"),
        )
    }

    @Test
    fun ready_withAFreeCredit_showsThePlainPage() {
        capture("ExportPreviewReady", readyState())
    }

    @Test
    fun docxSelected_namesTheDocxFile() {
        capture(
            "ExportPreviewDocx",
            readyState().copy(format = ExportFormat.DOCX, fileName = PDF_NAME.removeSuffix(".pdf") + ".docx"),
        )
    }

    @Test
    fun noCredit_saysDownloadingOpensThePack() {
        capture("ExportPreviewNoCredit", readyState().copy(freeCredits = 0))
    }

    @Test
    fun paidCredits_saysHowManyAreLeft() {
        capture("ExportPreviewPaidCredits", readyState().copy(freeCredits = 0, purchasedCredits = 5))
    }

    @Test
    fun freeBeta_saysDownloadsAreFree() {
        capture("ExportPreviewFreeBeta", readyState().copy(isFreeBeta = true))
    }

    @Test
    fun exporting_saysNothingIsChargedUntilTheFileIsReady() {
        capture("ExportPreviewExporting", readyState().copy(stage = ExportPreviewStage.EXPORTING))
    }

    @Test
    fun renderError_saysNoCreditWasUsed() {
        capture("ExportPreviewError", readyState().copy(stage = ExportPreviewStage.PREVIEW_FAILED, sheet = null))
    }

    @Test
    fun offline_keepsThePreviewAndDisablesTheDownload() {
        capture("ExportPreviewOffline", readyState().copy(isOffline = true))
    }

    @Test
    fun noDocument_asksTheUserToReviewFirst() {
        capture("ExportPreviewNoDocument", readyState().copy(stage = ExportPreviewStage.NO_DOCUMENT, sheet = null))
    }

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun ready_atLargeTextKeepsTheControlsUsable() {
        capture("ExportPreviewReadyFont200", readyState(), device = TmrTestDevices.boardLargeFont)
    }

    private fun capture(
        screenName: String,
        uiState: ExportPreviewUiState,
        device: TmrTestDevice = TmrTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent { ExportPreviewHost(uiState = uiState, dark = darkTheme.value) }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

@Composable
private fun ExportPreviewHost(
    uiState: ExportPreviewUiState,
    dark: Boolean,
) {
    TmrTheme(darkTheme = dark) {
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
