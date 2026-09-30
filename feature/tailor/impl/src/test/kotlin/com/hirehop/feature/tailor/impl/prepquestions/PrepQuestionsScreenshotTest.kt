package com.hirehop.feature.tailor.impl.prepquestions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.prep.GeneratePrepQuestionsUseCase
import com.hirehop.core.domain.prep.PrepQuestionGenerator
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.data.canonicalProfileWithoutEntries
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
    fun generating_readsInLightAndDark() {
        capture("PrepQuestionsGenerating", PrepQuestionsUiState())
    }

    @Test
    fun readyWithAllThreeKinds_readsInLightAndDark() {
        capture("PrepQuestionsReady", readyState())
    }

    @Test
    fun gapsOnly_readsInLightAndDark() {
        capture("PrepQuestionsGapsOnly", readyState().copy(filter = PrepQuestionFilter.GAP))
    }

    @Test
    fun oneGroupAtATime_readsInLightAndDark() {
        capture("PrepQuestionsStrengthsOnly", readyState().copy(filter = PrepQuestionFilter.STRENGTH))
    }

    @Test
    fun practisedQuestion_readsInLightAndDark() {
        capture("PrepQuestionsPractised", practisedState())
    }

    @Test
    fun emptyAnalysis_readsInLightAndDark() {
        capture("PrepQuestionsEmptyAnalysis", emptyAnalysisState())
    }

    @Test
    fun emptyProfile_readsInLightAndDark() {
        capture("PrepQuestionsEmptyProfile", emptyProfileState())
    }

    @Test
    fun offline_readsInLightAndDark() {
        capture("PrepQuestionsOffline", readyState().copy(isOffline = true, stage = PrepQuestionsStage.OFFLINE))
    }

    @Test
    fun error_readsInLightAndDark() {
        capture(
            "PrepQuestionsError",
            PrepQuestionsUiState(
                stage = PrepQuestionsStage.ERROR,
                jobTitle = "Associate Android Engineer",
                jobCompany = "Northwind GCC",
            ),
        )
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun readyWithAllThreeKinds_atLargeTextStacksFullWidth() {
        capture("PrepQuestionsReadyFont200", readyState(), device = HhTestDevices.boardLargeFont)
    }

    private fun capture(
        screenName: String,
        uiState: PrepQuestionsUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            PrepQuestionsHost(uiState = uiState, dark = darkTheme.value)
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

@Composable
private fun PrepQuestionsHost(
    uiState: PrepQuestionsUiState,
    dark: Boolean,
) {
    com.hirehop.core.designsystem.theme.HhTheme(darkTheme = dark) {
        PrepQuestionsScreen(
            uiState = uiState,
            actions = PrepQuestionsActions(
                onFilterChosen = {},
                onPractiseToggled = {},
                onReportInaccurate = {},
                onDismissMessage = {},
                onRetry = {},
                onNavigateBack = {},
            ),
        )
    }
}

private val generator = GeneratePrepQuestionsUseCase()

private fun stateFor(
    gap: GapAnalysis,
    profile: CandidateProfile,
    isOffline: Boolean = false,
): PrepQuestionsUiState {
    val analysis = JobAnalysisResult(job = canonicalApplication.job, gap = gap)
    val questions = runBlocking {
        generator(
            analysis = analysis,
            profile = profile,
            limit = PrepQuestionGenerator.MAX_QUESTIONS,
        )
    }
    return prepQuestionsStateFor(
        PrepQuestionsInputs(
            profile = profile,
            questions = questions,
            jobTitle = analysis.job.title,
            jobCompany = analysis.job.company,
            isOffline = isOffline,
        ),
    )
}

private fun readyState(): PrepQuestionsUiState = stateFor(
    gap = requireNotNull(canonicalApplication.gapAnalysis),
    profile = canonicalCandidateProfile,
)

private fun emptyAnalysisState(): PrepQuestionsUiState = stateFor(
    gap = GapAnalysis(matches = emptyList(), keywordCoverage = KeywordCoverage(covered = 0, total = 0)),
    profile = canonicalCandidateProfile,
)

private fun emptyProfileState(): PrepQuestionsUiState = stateFor(
    gap = requireNotNull(canonicalApplication.gapAnalysis),
    profile = canonicalProfileWithoutEntries,
)

private fun practisedState(): PrepQuestionsUiState {
    val ready = readyState()
    return ready.copy(
        groups = ready.groups.map { group ->
            group.copy(
                cards = group.cards.mapIndexed { index, card ->
                    if (index == 0) card.copy(isPractised = true) else card
                },
            )
        },
    )
}
