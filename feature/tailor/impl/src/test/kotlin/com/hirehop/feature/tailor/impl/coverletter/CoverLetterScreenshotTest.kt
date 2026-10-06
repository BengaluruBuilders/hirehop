package com.hirehop.feature.tailor.impl.coverletter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
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
    fun offer_showsWriteOneAndNoThanksAtEqualWeight() {
        capture("CoverLetterOffer", offerState())
    }

    @Test
    fun generating_showsNamedSteps() {
        capture("CoverLetterGenerating", offerState().copy(stage = CoverLetterStage.GENERATING))
    }

    @Test
    fun ready_showsPerParagraphCitations() {
        capture("CoverLetterReady", readyState())
    }

    @Test
    fun sources_listsTheFactsBehindAParagraph() {
        captureSheet("CoverLetterSources", readyState()) {
            val state = readyState()
            val paragraph = state.paragraphs.first { it.basis == CoverLetterBasis.CONFIRMED_FACT }
            ParagraphSources(position = state.positionOfParagraph(paragraph), paragraph = paragraph, onClose = {})
        }
    }

    @Test
    fun flaggedParagraph_asksTheCandidateToCheck() {
        capture(
            "CoverLetterFlagged",
            readyState().withParagraph { paragraph ->
                paragraph.copy(flag = CoverLetterFlag(quote = "steady practice … on a fixed weekly schedule", factId = "P-03"))
            },
        )
    }

    @Test
    fun editingInPlace_showsTheHandEditNote() {
        val ready = readyState()
        val target = ready.paragraphs.first { it.basis == CoverLetterBasis.CONFIRMED_FACT }
        capture(
            "CoverLetterEditing",
            ready.copy(editingOrdinal = target.ordinal, editingText = target.text),
        )
    }

    @Test
    fun userEdited_marksTheParagraphAsYours() {
        capture("CoverLetterUserEdited", editedState())
    }

    @Test
    fun error_offersTryAgainOrSkip() {
        capture(
            "CoverLetterError",
            CoverLetterUiState(
                stage = CoverLetterStage.ERROR,
                jobTitle = "Associate Analyst",
                jobCompany = "Northwind GCC",
            ),
        )
    }

    @Test
    fun offline_keepsTheLetterReadable() {
        capture("CoverLetterOffline", readyState().copy(isOffline = true))
    }

    @Test
    fun noMatchingEvidence_readsAsAnExplanationNotAnError() {
        capture("CoverLetterNoMatchingEvidence", noEvidenceState())
    }

    @Test
    fun emptyProfile_asksForAFact() {
        capture("CoverLetterEmptyProfile", emptyProfileState())
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun ready_atLargeTextStacksFullWidth() {
        capture("CoverLetterReadyFont200", readyState(), device = HhTestDevices.boardLargeFont)
    }

    private fun capture(
        screenName: String,
        uiState: CoverLetterUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                CoverLetterScreen(uiState = uiState, actions = noActions())
            }
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }

    private fun captureSheet(
        screenName: String,
        uiState: CoverLetterUiState,
        content: @Composable () -> Unit,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                Box(Modifier.fillMaxSize()) {
                    CoverLetterScreen(uiState = uiState, actions = noActions())
                    Box(Modifier.fillMaxSize().background(HhTheme.colors.scrim))
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(HhTheme.colors.surface, HhTheme.shapes.sheet)
                            .padding(
                                start = HhTheme.spacing.gutter,
                                end = HhTheme.spacing.gutter,
                                top = HhTheme.spacing.xl,
                                bottom = HhTheme.spacing.xxl,
                            ),
                    ) { content() }
                }
            }
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, HhTestDevices.board) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

private fun noActions() = CoverLetterActions(
    onWriteOne = {},
    onBeginEdit = {},
    onEditTextChanged = {},
    onSaveEdit = {},
    onCancelEdit = {},
    onReportInaccurate = {},
    onDismissMessage = {},
    onRetry = {},
    onNavigateBack = {},
    onSkipLetter = {},
    onPreviewExport = {},
    onPrepQuestions = {},
)

private fun CoverLetterUiState.positionOfParagraph(paragraph: CoverLetterParagraph): Int =
    paragraphs.filter { !it.isGreeting }.indexOfFirst { it.ordinal == paragraph.ordinal } + 1

private fun CoverLetterUiState.withParagraph(
    change: (CoverLetterParagraph) -> CoverLetterParagraph,
): CoverLetterUiState = copy(
    paragraphs = paragraphs.map { paragraph ->
        if (paragraph.basis == CoverLetterBasis.CONFIRMED_FACT) change(paragraph) else paragraph
    },
)

private val generator = GenerateCoverLetterUseCase()

private fun stateFor(gap: GapAnalysis, profile: CandidateProfile): CoverLetterUiState {
    val analysis = JobAnalysisResult(job = canonicalApplication.job, gap = gap)
    val draft = runBlocking { generator(candidate = profile, job = analysis.job, analysis = analysis) }
    return coverLetterStateFor(CoverLetterInputs(profile = profile, analysis = analysis, draft = draft))
        .copy(reviewedCount = 7, totalCount = 7)
}

private fun offerState() = CoverLetterUiState(
    stage = CoverLetterStage.OFFER,
    jobTitle = "Associate Analyst",
    jobCompany = "Northwind GCC",
    exportedFileName = "Priya_Sharma_Northwind_GCC_Associate_Analyst.pdf",
    reviewedCount = 7,
    totalCount = 7,
)

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

private fun editedState(): CoverLetterUiState = readyState().withParagraph { paragraph ->
    paragraph.copy(
        text = "I built these myself and can walk you through each one.",
        sentences = listOf(
            CoverLetterSentence(text = "I built these myself and can walk you through each one.", factId = null),
        ),
        isUserEdited = true,
    )
}
