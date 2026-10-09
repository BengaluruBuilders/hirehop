package com.tailormyresume.feature.tailor.impl.coverletter

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.coverletter.GenerateCoverLetterUseCase
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class CoverLetterEditedChipsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun editedParagraphShowsNoFactChips() {
        val ready = provenanceReadyState()
        val target = ready.firstCitedParagraph()
        val edited = ready.withProvenanceParagraph(target.ordinal) { paragraph ->
            paragraph.copy(isUserEdited = true)
        }

        composeRule.setContent {
            TmrTheme {
                CoverLetterScreen(uiState = edited, actions = provenanceNoActions())
            }
        }

        target.facts.forEach { fact ->
            assertThat(composeRule.onAllNodes(hasText(fact.displayId)).fetchSemanticsNodes()).isEmpty()
        }
    }

    @Test
    fun unEditedParagraphKeepsFactChips() {
        val ready = provenanceReadyState()
        val target = ready.firstCitedParagraph()

        composeRule.setContent {
            TmrTheme {
                CoverLetterScreen(uiState = ready, actions = provenanceNoActions())
            }
        }

        val firstFactDisplayId = target.facts.first().displayId
        assertThat(
            composeRule.onAllNodes(hasText(firstFactDisplayId)).fetchSemanticsNodes(),
        ).isNotEmpty()
    }

    @Test
    fun editingBlockShowsNoFactChips() {
        val ready = provenanceReadyState()
        val target = ready.firstCitedParagraph()

        composeRule.setContent {
            TmrTheme {
                CoverLetterScreen(
                    uiState = ready.copy(editingOrdinal = target.ordinal, editingText = target.text),
                    actions = provenanceNoActions(),
                )
            }
        }

        target.facts.forEach { fact ->
            assertThat(composeRule.onAllNodes(hasText(fact.displayId)).fetchSemanticsNodes()).isEmpty()
        }
    }

    private fun provenanceNoActions() = CoverLetterActions(
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
}

private val provenanceGenerator = GenerateCoverLetterUseCase()

private fun provenanceStateFor(gap: GapAnalysis, profile: CandidateProfile): CoverLetterUiState {
    val analysis = JobAnalysisResult(job = canonicalApplication.job, gap = gap)
    val draft = runBlocking { provenanceGenerator(candidate = profile, job = analysis.job, analysis = analysis) }
    return coverLetterStateFor(CoverLetterInputs(profile = profile, analysis = analysis, draft = draft))
        .copy(reviewedCount = 7, totalCount = 7)
}

private fun provenanceReadyState(): CoverLetterUiState = provenanceStateFor(
    gap = requireNotNull(canonicalApplication.gapAnalysis),
    profile = canonicalCandidateProfile,
)

private fun CoverLetterUiState.firstCitedParagraph(): CoverLetterParagraph =
    paragraphs.first { paragraph ->
        paragraph.basis == CoverLetterBasis.CONFIRMED_FACT && paragraph.facts.isNotEmpty()
    }

private fun CoverLetterUiState.withProvenanceParagraph(
    ordinal: Int,
    change: (CoverLetterParagraph) -> CoverLetterParagraph,
): CoverLetterUiState = copy(
    paragraphs = paragraphs.map { paragraph -> if (paragraph.ordinal == ordinal) change(paragraph) else paragraph },
)
