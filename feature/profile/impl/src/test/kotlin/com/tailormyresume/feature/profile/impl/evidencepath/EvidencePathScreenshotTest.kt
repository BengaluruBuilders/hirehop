package com.tailormyresume.feature.profile.impl.evidencepath

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.screenshot.TmrTestDevice
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class EvidencePathScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun picker() = capture("EvidencePathPicker", EvidencePathUiState())

    @Test
    fun question() = capture(
        "EvidencePathQuestion",
        EvidencePathUiState(category = EvidenceCategory.PROJECTS),
    )

    @Test
    fun TYPED_ANSWER() = capture(
        "EvidencePathTypedAnswer",
        EvidencePathUiState(category = EvidenceCategory.PROJECTS, answer = TYPED_ANSWER),
    )

    @Test
    fun cardStampedUserStated() = capture(
        "EvidencePathStamped",
        EvidencePathUiState(
            category = EvidenceCategory.PROJECTS,
            questionIndex = 1,
            cards = listOf(EvidenceFactCard(EvidenceCategory.PROJECTS, dashboard)),
        ),
    )

    @Test
    fun skipped() = capture(
        "EvidencePathSkipped",
        EvidencePathUiState(
            category = EvidenceCategory.WORK,
            skipNote = EvidenceSkipNote(EvidenceCategory.PROJECTS, 2),
            visited = setOf(EvidenceCategory.PROJECTS),
        ),
    )

    @Test
    fun allDone() = capture(
        "EvidencePathAllDone",
        EvidencePathUiState(
            isDone = true,
            cards = listOf(
                EvidenceFactCard(EvidenceCategory.PROJECTS, dashboard),
                EvidenceFactCard(EvidenceCategory.WORK, entry("I-01", "Weekly sales reports, 40 stores")),
                EvidenceFactCard(EvidenceCategory.COURSEWORK, entry("U-01", "DBMS: SQL joins, GROUP BY")),
                EvidenceFactCard(EvidenceCategory.COMPETITIONS, entry("P-01", "Smart India Hackathon 2024")),
                EvidenceFactCard(EvidenceCategory.POSITIONS, entry("P-02", "Treasurer, coding club")),
            ),
        ),
    )

    @Test
    fun nothingAdded() = capture("EvidencePathNothingAdded", EvidencePathUiState(isDone = true))

    @Test
    fun offline() = capture(
        "EvidencePathOffline",
        EvidencePathUiState(isOffline = true, category = EvidenceCategory.PROJECTS, answer = TYPED_ANSWER),
    )

    @Test
    fun saveFailed() = capture(
        "EvidencePathSaveFailed",
        EvidencePathUiState(
            category = EvidenceCategory.PROJECTS,
            answer = TYPED_ANSWER,
            message = EvidenceMessage.SAVE_FAILED,
        ),
    )

    @Test
    fun loading() = capture("EvidencePathLoading", EvidencePathUiState(isLoading = true))

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun picker_atLargeText() = capture(
        "EvidencePathPickerFont200",
        EvidencePathUiState(),
        device = TmrTestDevices.boardLargeFont,
    )

    private fun capture(
        screenName: String,
        uiState: EvidencePathUiState,
        device: TmrTestDevice = TmrTestDevices.board,
    ) = runBlocking {
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                EvidencePathScreen(uiState = uiState, actions = EvidencePathActions.None, onBack = {})
            }
        }
        composeRule.captureMultiTheme(
            outputDirectory = SCREENSHOT_DIRECTORY,
            screenName = screenName,
            device = device,
            setTheme = { dark -> darkTheme.value = dark },
        )
        Unit
    }

    private companion object {
        const val SCREENSHOT_DIRECTORY = "src/test/screenshots"
        const val TYPED_ANSWER =
            "Placement Stats Dashboard. Power BI and Excel. The T&P cell used it for the 2024 placement report."

        val dashboard = entry(
            id = "C-02",
            title = "Placement Stats Dashboard",
            detail = "Power BI and Excel. The T&P cell used it for the 2024 placement report.",
        )

        fun entry(id: String, title: String, detail: String = "") = ProfileEntry(
            id = id,
            category = EntryCategory.PROJECT,
            title = title,
            organization = "",
            startDate = "",
            endDate = "",
            bullets = if (detail.isEmpty()) emptyList() else listOf(EvidenceBullet(id = "$id-b", text = detail)),
            source = FactSource.USER_STATED,
            isConfirmed = true,
        )
    }
}
