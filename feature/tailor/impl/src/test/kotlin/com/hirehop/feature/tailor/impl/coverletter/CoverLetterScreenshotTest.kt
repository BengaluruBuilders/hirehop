package com.hirehop.feature.tailor.impl.coverletter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.coverletter.GenerateCoverLetterUseCase
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.MatchStatus
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
class CoverLetterScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun generating_readsInLightAndDark() {
        capture("CoverLetterGenerating", CoverLetterUiState())
    }

    @Test
    fun readyLetter_readsInLightAndDark() {
        capture("CoverLetterReady", readyState())
    }

    @Test
    fun noMatchingEvidence_readsAsAnExplanationNotAnError() {
        capture("CoverLetterNoMatchingEvidence", noEvidenceState())
    }

    @Test
    fun emptyProfile_readsInLightAndDark() {
        capture("CoverLetterEmptyProfile", emptyProfileState())
    }

    @Test
    fun offline_readsInLightAndDark() {
        capture(
            "CoverLetterOffline",
            readyState().copy(isOffline = true, stage = CoverLetterStage.OFFLINE),
        )
    }

    @Test
    fun error_readsInLightAndDark() {
        capture(
            "CoverLetterError",
            CoverLetterUiState(
                stage = CoverLetterStage.ERROR,
                jobTitle = "Associate Android Engineer",
                jobCompany = "Northwind GCC",
            ),
        )
    }

    @Test
    fun editedParagraph_readsInLightAndDark() {
        capture("CoverLetterUserEdited", editedState())
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun readyLetter_atLargeTextStacksFullWidth() {
        capture("CoverLetterReadyFont200", readyState(), device = HhTestDevices.boardLargeFont)
    }

    private fun capture(
        screenName: String,
        uiState: CoverLetterUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            CoverLetterHost(uiState = uiState, dark = darkTheme.value)
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

@Composable
private fun CoverLetterHost(
    uiState: CoverLetterUiState,
    dark: Boolean,
) {
    com.hirehop.core.designsystem.theme.HhTheme(darkTheme = dark) {
        CoverLetterScreen(
            uiState = uiState,
            actions = CoverLetterActions(
                onBeginEdit = {},
                onEditTextChanged = {},
                onSaveEdit = {},
                onCancelEdit = {},
                onCopyLetter = {},
                onReportInaccurate = {},
                onDismissMessage = {},
                onRetry = {},
                onNavigateBack = {},
                onSkipLetter = {},
            ),
        )
    }
}

private val generator = GenerateCoverLetterUseCase()

private fun stateFor(
    gap: GapAnalysis,
    profile: CandidateProfile,
    isOffline: Boolean = false,
): CoverLetterUiState {
    val analysis = JobAnalysisResult(job = canonicalApplication.job, gap = gap)
    val draft = runBlocking { generator(candidate = profile, job = analysis.job, analysis = analysis) }
    return coverLetterStateFor(
        CoverLetterInputs(
            profile = profile,
            analysis = analysis,
            draft = draft,
            isOffline = isOffline,
        ),
    )
}

private fun readyState(): CoverLetterUiState = stateFor(
    gap = requireNotNull(canonicalApplication.gapAnalysis),
    profile = canonicalCandidateProfile,
)

private fun noEvidenceState(): CoverLetterUiState {
    val base = requireNotNull(canonicalApplication.gapAnalysis)
    return stateFor(
        gap = base.copy(
            matches = base.matches.map { match ->
                match.copy(status = MatchStatus.GAP, evidenceIds = emptyList())
            },
        ),
        profile = canonicalCandidateProfile,
    )
}

private fun emptyProfileState(): CoverLetterUiState = stateFor(
    gap = requireNotNull(canonicalApplication.gapAnalysis),
    profile = canonicalProfileWithoutEntries,
)

private fun editedState(): CoverLetterUiState {
    val ready = readyState()
    return ready.copy(
        paragraphs = ready.paragraphs.map { paragraph ->
            if (paragraph.basis == CoverLetterBasis.CONFIRMED_FACT) {
                paragraph.copy(
                    text = "I built these myself and can walk you through each one.",
                    sentences = listOf(
                        CoverLetterSentence(
                            text = "I built these myself and can walk you through each one.",
                            factId = null,
                        ),
                    ),
                    isUserEdited = true,
                )
            } else {
                paragraph
            }
        },
    )
}
