package com.hirehop.feature.profile.impl.guidedform

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.EntryCategory
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
    fun scannedArrival() = capture(
        "GuidedFormScannedArrival",
        GuidedFormUiState(arrival = GuidedArrival.FROM_SCANNED_PDF, showIntro = true),
    )

    @Test
    fun contactStep() = capture(
        "GuidedFormContact",
        GuidedFormUiState(values = contactValues),
    )

    @Test
    fun educationStep() = capture(
        "GuidedFormEducation",
        GuidedFormUiState(stepIndex = 1, values = educationValues, completedSteps = setOf(GuidedStep.CONTACT)),
    )

    @Test
    fun educationStepWithProblem() = capture(
        "GuidedFormEducationProblem",
        GuidedFormUiState(
            stepIndex = 1,
            values = mapOf(GuidedField.COLLEGE to "Savitribai Phule Pune University"),
            fieldProblems = mapOf(GuidedField.COURSE to GuidedFieldProblem.REQUIRED),
        ),
    )

    @Test
    fun skillsStepWithFiledEducation() = capture(
        "GuidedFormSkillsFiled",
        GuidedFormUiState(
            stepIndex = 2,
            skills = listOf("SQL", "Excel", "Power BI"),
            filedEntries = listOf(
                filed("U-01", "B.Tech Computer Science, Savitribai Phule Pune University, 2024"),
                filed("U-02", "DBMS, Probability and Statistics"),
            ),
        ),
    )

    @Test
    fun skillsStep() = capture(
        "GuidedFormSkills",
        GuidedFormUiState(stepIndex = 2, skills = listOf("SQL", "Excel", "Power BI")),
    )

    @Test
    fun experienceStepHandsOffToTheEvidencePath() = capture(
        "GuidedFormExperienceHandoff",
        GuidedFormUiState(stepIndex = 3),
    )

    @Test
    fun savedForLater() = capture(
        "GuidedFormSavedForLater",
        GuidedFormUiState(
            saved = GuidedSaved(completedSteps = 2, totalSteps = 4, entryIds = listOf("U-01", "U-02")),
        ),
    )

    @Test
    fun offline() = capture(
        "GuidedFormOffline",
        GuidedFormUiState(stepIndex = 1, isOffline = true, values = educationValues),
    )

    @Test
    fun loading() = capture("GuidedFormLoading", GuidedFormUiState(isLoading = true))

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun educationStep_atLargeText() = capture(
        "GuidedFormEducationFont200",
        GuidedFormUiState(stepIndex = 1, values = educationValues),
        device = HhTestDevices.boardLargeFont,
    )

    private fun capture(
        screenName: String,
        uiState: GuidedFormUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                GuidedFormScreen(uiState = uiState, actions = GuidedFormActions.None, onBack = {})
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

        val contactValues = mapOf(
            GuidedField.FULL_NAME to "Priya Deshmukh",
            GuidedField.EMAIL to "priya.d@example.com",
            GuidedField.PHONE to "+91 98220 41873",
        )

        val educationValues = mapOf(
            GuidedField.COURSE to "B.Tech Computer Science",
            GuidedField.COLLEGE to "Savitribai Phule Pune University",
            GuidedField.EDUCATION_END to "2024",
            GuidedField.COURSEWORK to "DBMS, Probability and Statistics",
        )

        fun filed(id: String, title: String) = ProfileEntry(
            id = id,
            category = EntryCategory.EDUCATION,
            title = title,
            organization = "",
            startDate = "",
            endDate = "",
            bullets = emptyList(),
            source = FactSource.USER_STATED,
            isConfirmed = true,
        )
    }
}
