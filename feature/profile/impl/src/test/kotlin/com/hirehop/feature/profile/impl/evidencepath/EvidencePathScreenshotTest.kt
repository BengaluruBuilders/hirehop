package com.hirehop.feature.profile.impl.evidencepath

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class EvidencePathScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun categoryPicker_readsInLightAndDark() {
        captureBothThemes(
            screenName = "EvidencePathPicker",
            uiState = pickerState(),
        )
    }

    @Test
    fun question_readsInLightAndDark() {
        captureBothThemes(
            screenName = "EvidencePathQuestion",
            uiState = questionState(),
        )
    }

    @Test
    fun typedAnswer_foldsIntoAFactCardStampedUserStated() {
        captureBothThemes(
            screenName = "EvidencePathAnswerFolded",
            uiState = foldedState(),
        )
    }

    @Test
    fun skipNote_readsInLightAndDark() {
        captureBothThemes(
            screenName = "EvidencePathSkipped",
            uiState = pickerState(skipped = listOf(EvidenceCategory.PROJECTS, EvidenceCategory.INTERNSHIPS)),
        )
    }

    @Test
    fun allDone_readsInLightAndDark() {
        captureBothThemes(
            screenName = "EvidencePathAllDone",
            uiState = foldedState(done = EvidenceDone(addedCount = 5, skippedCount = 1)),
        )
    }

    @Test
    fun nothingAdded_readsInLightAndDark() {
        captureBothThemes(
            screenName = "EvidencePathNothingAdded",
            uiState = pickerState(done = EvidenceDone(addedCount = 0, skippedCount = 3)),
        )
    }

    @Test
    fun saveRejected_readsInLightAndDark() {
        captureBothThemes(
            screenName = "EvidencePathSaveRejected",
            uiState = questionState(
                answers = mapOf(EvidencePrompt.TITLE to ""),
                problems = mapOf(EvidencePrompt.TITLE to EvidenceFieldProblem.REQUIRED),
                isSaveRejected = true,
                message = EvidenceMessage.SAVE_REJECTED,
            ),
        )
    }

    @Test
    fun offlineState_readsInLightAndDark() {
        captureBothThemes(
            screenName = "EvidencePathOffline",
            uiState = questionState(isOffline = true),
        )
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun categoryPicker_atLargeTextWrapsTheChips() {
        captureBothThemes(
            screenName = "EvidencePathPickerFont200",
            uiState = pickerState(),
            device = HhTestDevices.boardLargeFont,
        )
    }

    @Test
    fun loadingState_readsInLightAndDark() {
        captureBothThemes(
            screenName = "EvidencePathLoading",
            uiState = EvidencePathUiState(isLoading = true),
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: EvidencePathUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        showScreen(uiState)
        composeRule.captureMultiTheme(
            outputDirectory = OUTPUT,
            screenName = screenName,
            device = device,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }

    private fun showScreen(uiState: EvidencePathUiState) {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                EvidencePathScreen(uiState = uiState, actions = noOpActions)
            }
        }
        composeRule.waitForIdle()
    }

    private val noOpActions = EvidencePathActions(
        onCategoryChosen = {},
        onAnswerChanged = { _, _ -> },
        onNextPrompt = {},
        onBackPrompt = {},
        onSkipPrompt = {},
        onSave = {},
        onSkipCategory = {},
        onAddMore = {},
        onGoToProfile = {},
        onDismissMessage = {},
    )

    private fun pickerState(
        isOffline: Boolean = false,
        cards: List<EvidenceFactCard> = emptyList(),
        skipped: List<EvidenceCategory> = emptyList(),
        done: EvidenceDone? = null,
    ) = EvidencePathUiState(
        isOffline = isOffline,
        cards = cards,
        skipped = skipped,
        done = done,
    )

    private fun questionState(
        isOffline: Boolean = false,
        answers: Map<EvidencePrompt, String> = emptyMap(),
        problems: Map<EvidencePrompt, EvidenceFieldProblem> = emptyMap(),
        isSaveRejected: Boolean = false,
        message: EvidenceMessage? = null,
    ) = EvidencePathUiState(
        isOffline = isOffline,
        category = EvidenceCategory.PROJECTS,
        question = EvidenceQuestion(
            category = EvidenceCategory.PROJECTS,
            promptIndex = 0,
            totalPrompts = 2,
            answers = answers,
            problems = problems,
        ),
        isSaveRejected = isSaveRejected,
        message = message,
    )

    private fun foldedState(done: EvidenceDone? = null) = EvidencePathUiState(
        cards = listOf(
            EvidenceFactCard(
                category = EvidenceCategory.PROJECTS,
                entryCategory = EntryCategory.PROJECT,
                line = "Placement Stats Dashboard · Power BI and Excel. The T and P cell used it for the " +
                    "2024 placement report.",
                answer = "Placement Stats Dashboard",
                entry = projectEntry,
            ),
        ),
        done = done,
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"

        val projectEntry = ProfileEntry(
            id = "C-01",
            category = EntryCategory.PROJECT,
            title = "Placement Stats Dashboard",
            organization = "",
            startDate = "",
            endDate = "",
            bullets = listOf(
                EvidenceBullet(
                    id = "b-1",
                    text = "Power BI and Excel. The T and P cell used it for the 2024 placement report.",
                ),
            ),
            source = FactSource.USER_STATED,
            isConfirmed = false,
        )
    }
}
