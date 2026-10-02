package com.hirehop.feature.tailor.impl.prepquestions

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.prep.GeneratePrepQuestionsUseCase
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
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
class PrepQuestionsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun generating_showsNamedSteps() {
        capture("PrepQuestionsGenerating", PrepQuestionsUiState(jobTitle = "Associate Analyst", jobCompany = "Northwind GCC"))
    }

    @Test
    fun ready_listsQuestionsWithTheirFacts() {
        capture("PrepQuestionsReady", readyState())
    }

    @Test
    fun gaps_showsHonestGuidanceUnderToPrepare() {
        val ready = readyState()
        capture("PrepQuestionsGaps", ready.copy(groups = ready.groups.filter { it.kind.name == "GAP" }))
    }

    @Test
    fun reportedQuestion_showsReportedInPlaceOfTheReportAction() {
        val ready = readyState()
        capture(
            "PrepQuestionsReported",
            ready.copy(reportedIds = setOf(ready.groups.first().cards.first().id), message = PrepQuestionsMessage.REPORTED),
        )
    }

    @Test
    fun error_offersTryAgain() {
        capture(
            "PrepQuestionsError",
            PrepQuestionsUiState(stage = PrepQuestionsStage.ERROR, jobTitle = "Associate Analyst", jobCompany = "Northwind GCC"),
        )
    }

    @Test
    fun offline_keepsTheSavedListReadable() {
        capture("PrepQuestionsOffline", readyState().copy(isOffline = true))
    }

    @Test
    fun emptyAnalysis_saysThereIsNothingToPrepare() {
        capture("PrepQuestionsEmptyAnalysis", PrepQuestionsUiState(stage = PrepQuestionsStage.EMPTY_ANALYSIS))
    }

    @Test
    fun emptyProfile_asksForAFact() {
        capture("PrepQuestionsEmptyProfile", PrepQuestionsUiState(stage = PrepQuestionsStage.EMPTY_PROFILE))
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun ready_atLargeTextStacksFullWidth() {
        capture("PrepQuestionsReadyFont200", readyState(), device = HhTestDevices.boardLargeFont)
    }

    private fun capture(
        screenName: String,
        uiState: PrepQuestionsUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                PrepQuestionsScreen(
                    uiState = uiState,
                    actions = PrepQuestionsActions(
                        onReportInaccurate = {},
                        onDismissMessage = {},
                        onRetry = {},
                        onOpenPrepPlan = {},
                        onEditFact = { _, _ -> },
                        onNavigateBack = {},
                    ),
                )
            }
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

private fun stateFor(gap: GapAnalysis, profile: CandidateProfile): PrepQuestionsUiState {
    val analysis = JobAnalysisResult(job = canonicalApplication.job, gap = gap)
    val questions = runBlocking { GeneratePrepQuestionsUseCase()(analysis = analysis, profile = profile) }
    return prepQuestionsStateFor(
        PrepQuestionsInputs(
            profile = profile,
            questions = questions,
            jobTitle = "Associate Analyst",
            jobCompany = "Northwind GCC",
        ),
    )
}

private fun readyState(): PrepQuestionsUiState = stateFor(
    gap = requireNotNull(canonicalApplication.gapAnalysis),
    profile = canonicalCandidateProfile,
)
