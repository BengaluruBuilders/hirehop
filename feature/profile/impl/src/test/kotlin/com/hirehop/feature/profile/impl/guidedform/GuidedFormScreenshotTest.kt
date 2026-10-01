package com.hirehop.feature.profile.impl.guidedform

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
class GuidedFormScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun contactStep_readsInLightAndDark() {
        captureBothThemes(
            screenName = "GuidedFormContact",
            uiState = baseState(),
        )
    }

    @Test
    fun educationStep_readsInLightAndDark() {
        captureBothThemes(
            screenName = "GuidedFormEducation",
            uiState = baseState(
                stepIndex = 1,
                values = mapOf(
                    GuidedField.COURSE to "B.Tech Computer Science",
                    GuidedField.COLLEGE to "Example Institute of Technology, Pune",
                    GuidedField.EDUCATION_START to "2022",
                    GuidedField.EDUCATION_END to "2026",
                ),
                completedSteps = listOf(GuidedStep.CONTACT),
            ),
        )
    }

    @Test
    fun skillsStep_readsInLightAndDark() {
        captureBothThemes(
            screenName = "GuidedFormSkills",
            uiState = baseState(
                stepIndex = 2,
                values = mapOf(GuidedField.SKILL to "Kotlin\nSQL\nPower BI\nExcel"),
                completedSteps = listOf(GuidedStep.CONTACT, GuidedStep.EDUCATION),
            ),
        )
    }

    @Test
    fun experienceStep_reassuresAFresherAndOffersTheEvidencePath() {
        captureBothThemes(
            screenName = "GuidedFormExperienceHandoff",
            uiState = baseState(
                stepIndex = 3,
                completedSteps = listOf(GuidedStep.CONTACT, GuidedStep.EDUCATION, GuidedStep.SKILLS),
            ),
        )
    }

    @Test
    fun scannedArrival_readsInLightAndDark() {
        captureBothThemes(
            screenName = "GuidedFormScannedArrival",
            uiState = baseState(arrival = GuidedArrival.FROM_SCANNED_PDF),
        )
    }

    @Test
    fun offlineState_readsInLightAndDark() {
        captureBothThemes(
            screenName = "GuidedFormOffline",
            uiState = baseState(isOffline = true),
        )
    }

    @Test
    fun savedForLater_readsInLightAndDark() {
        captureBothThemes(
            screenName = "GuidedFormSavedForLater",
            uiState = baseState(
                stepIndex = 1,
                values = mapOf(GuidedField.COURSE to "B.Tech Computer Science"),
                previews = listOf(
                    GuidedFactPreview(
                        category = EntryCategory.EDUCATION,
                        line = "B.Tech Computer Science · Example Institute of Technology, Pune · 2022 to 2026",
                        entry = educationEntry,
                    ),
                ),
                completedSteps = listOf(GuidedStep.CONTACT),
                saved = GuidedSaved(completedSteps = 1, totalSteps = GUIDED_STEPS.size),
                message = GuidedMessage.SAVED,
            ),
        )
    }

    @Test
    fun saveRejected_readsInLightAndDark() {
        captureBothThemes(
            screenName = "GuidedFormSaveRejected",
            uiState = baseState(
                stepIndex = 1,
                fieldProblems = mapOf(GuidedField.COURSE to GuidedFieldProblem.REQUIRED),
                isSaveRejected = true,
                message = GuidedMessage.SAVE_REJECTED,
            ),
        )
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun educationStep_atLargeTextStacksFullWidth() {
        captureBothThemes(
            screenName = "GuidedFormEducationFont200",
            uiState = baseState(
                stepIndex = 1,
                values = mapOf(
                    GuidedField.COURSE to "B.Tech Computer Science",
                    GuidedField.COLLEGE to "Example Institute of Technology, Pune",
                ),
                completedSteps = listOf(GuidedStep.CONTACT),
            ),
            device = HhTestDevices.boardLargeFont,
        )
    }

    @Test
    fun loadingState_readsInLightAndDark() {
        captureBothThemes(
            screenName = "GuidedFormLoading",
            uiState = baseState(isLoading = true),
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: GuidedFormUiState,
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

    private fun showScreen(uiState: GuidedFormUiState) {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                GuidedFormScreen(uiState = uiState, actions = noOpActions)
            }
        }
        composeRule.waitForIdle()
    }

    private val noOpActions = GuidedFormActions(
        onValueChange = { _, _ -> },
        onNext = {},
        onBack = {},
        onSaveAndFinishLater = {},
        onContinueNow = {},
        onStartHandoff = {},
        onDismissMessage = {},
    )

    private fun baseState(
        stepIndex: Int = 0,
        isLoading: Boolean = false,
        isOffline: Boolean = false,
        arrival: GuidedArrival = GuidedArrival.NORMAL,
        values: Map<GuidedField, String> = emptyMap(),
        fieldProblems: Map<GuidedField, GuidedFieldProblem> = emptyMap(),
        previews: List<GuidedFactPreview> = emptyList(),
        isSaveRejected: Boolean = false,
        completedSteps: List<GuidedStep> = emptyList(),
        saved: GuidedSaved? = null,
        message: GuidedMessage? = null,
    ) = GuidedFormUiState(
        isLoading = isLoading,
        isOffline = isOffline,
        arrival = arrival,
        stepIndex = stepIndex,
        values = values,
        fieldProblems = fieldProblems,
        previews = previews,
        isSaveRejected = isSaveRejected,
        completedSteps = completedSteps,
        saved = saved,
        message = message,
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"

        val educationEntry = ProfileEntry(
            id = "U-01",
            category = EntryCategory.EDUCATION,
            title = "B.Tech Computer Science",
            organization = "Example Institute of Technology, Pune",
            startDate = "2022",
            endDate = "2026",
            bullets = listOf(EvidenceBullet(id = "b-1", text = "Completed DBMS coursework.")),
            source = FactSource.USER_STATED,
            isConfirmed = false,
        )
    }
}
