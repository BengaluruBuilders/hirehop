package com.tailormyresume.feature.onboarding.impl.importresume

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.DebugScenario
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
class ImportResumeScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun idle_readsInLightAndDark() {
        captureBothThemes(screenName = "ImportResumeIdle", uiState = idleState())
    }

    @Test
    fun picking_readsInLightAndDark() {
        captureBothThemes(
            screenName = "ImportResumePicking",
            uiState = idleState().copy(stage = ImportStage.Picking),
        )
    }

    @Test
    fun readingStepTwoShowsTheFirstLiftedFacts() {
        captureBothThemes(
            screenName = "ImportResumeReadingStep2",
            uiState = ImportResumeUiState(
                stage = ImportStage.Parsing,
                fileName = "Priya_Deshmukh_Resume.pdf",
                byteSize = SAMPLE_SIZE,
                readStepIndex = 1,
                facts = ImportResumeScenarioMapper.seed(DebugScenario.SUCCESS).facts.take(2),
            ),
        )
    }

    @Test
    fun readingStepThreeShowsMoreLiftedFacts() {
        captureBothThemes(
            screenName = "ImportResumeReadingStep3",
            uiState = ImportResumeUiState(
                stage = ImportStage.Parsing,
                fileName = "Priya_Deshmukh_Resume.pdf",
                byteSize = SAMPLE_SIZE,
                readStepIndex = 2,
                facts = ImportResumeScenarioMapper.seed(DebugScenario.SUCCESS).facts,
            ),
        )
    }

    @Test
    fun success_readsInLightAndDark() {
        captureBothThemes(
            screenName = "ImportResumeSuccess",
            uiState = ImportResumeScenarioMapper.seed(DebugScenario.SUCCESS),
        )
    }

    @Test
    fun scannedPdfOffersTheGuidedFormAtEqualWeight() {
        captureBothThemes(
            screenName = "ImportResumeScannedNoText",
            uiState = ImportResumeScenarioMapper.seed(DebugScenario.SCANNED),
        )
    }

    @Test
    fun noFactsFoundIsCalm() {
        captureBothThemes(
            screenName = "ImportResumeNoFactsFound",
            uiState = ImportResumeScenarioMapper.seed(DebugScenario.SUCCESS)
                .copy(stage = ImportStage.NoFactsFound, facts = emptyList(), skillCount = 0),
        )
    }

    @Test
    fun unsupportedFileKeepsTheNeutralNote() {
        captureBothThemes(
            screenName = "ImportResumeUnsupported",
            uiState = idleState().copy(stage = ImportStage.Unsupported, fileName = "Resume.pages"),
        )
    }

    @Test
    fun tooLargeFileUsesTheErrorContainer() {
        captureBothThemes(
            screenName = "ImportResumeTooLarge",
            uiState = idleState().copy(stage = ImportStage.TooLarge, fileName = "Portfolio.pdf"),
        )
    }

    @Test
    fun emptyFileUsesTheErrorContainer() {
        captureBothThemes(
            screenName = "ImportResumeEmptyFile",
            uiState = idleState().copy(stage = ImportStage.Empty, fileName = "Resume.pdf"),
        )
    }

    @Test
    fun failedReadUsesTheErrorContainer() {
        captureBothThemes(
            screenName = "ImportResumeFailed",
            uiState = ImportResumeScenarioMapper.seed(DebugScenario.ERROR),
        )
    }

    @Test
    fun offlineQueuesTheFileOnThePhone() {
        captureBothThemes(
            screenName = "ImportResumeOffline",
            uiState = ImportResumeScenarioMapper.seed(DebugScenario.OFFLINE),
        )
    }

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun idleAtLargeTextStacksFullWidth() {
        captureBothThemes(
            screenName = "ImportResumeIdleFont200",
            uiState = idleState(),
            device = TmrTestDevices.boardLargeFont,
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: ImportResumeUiState,
        device: TmrTestDevice = TmrTestDevices.board,
    ) = runBlocking {
        showScreen(uiState)
        composeRule.captureMultiTheme(
            outputDirectory = OUTPUT,
            screenName = screenName,
            device = device,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }

    private fun showScreen(uiState: ImportResumeUiState) {
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                ImportResumeScreen(uiState = uiState, actions = noOpActions)
            }
        }
        composeRule.waitForIdle()
    }

    private val noOpActions = ImportResumeActions(
        onBack = {},
        onPickFile = {},
        onChooseAnotherFile = {},
        onRetry = {},
        onStartGuidedForm = {},
        onReviewFacts = {},
    )

    private fun idleState() = ImportResumeUiState()

    private companion object {
        const val OUTPUT = "src/test/screenshots"
        const val SAMPLE_SIZE = 212L * 1024L
    }
}
