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
class CoverLetterEditingProvenanceTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun editPanel_showsUserEditedChipAndNoBackedClaim() {
        val ready = provenanceReadyState()
        val target = ready.paragraphs.first { it.basis == CoverLetterBasis.CONFIRMED_FACT }

        composeRule.setContent {
            TmrTheme {
                CoverLetterScreen(
                    uiState = ready.copy(editingOrdinal = target.ordinal, editingText = target.text),
                    actions = provenanceNoActions(),
                )
            }
        }

        assertThat(
            composeRule.onAllNodes(hasText("User-edited")).fetchSemanticsNodes(),
        ).isNotEmpty()
        assertThat(
            composeRule.onAllNodes(hasText("Still backed by your facts", substring = true)).fetchSemanticsNodes(),
        ).isEmpty()
    }

    @Test
    fun savedHandEdit_showsUserEditedNoteAndNoBackedClaim() {
        val state = provenanceReadyState().withProvenanceParagraph { paragraph ->
            val editedText = paragraph.text + " I led a team of 40 engineers."
            paragraph.copy(
                text = editedText,
                sentences = listOf(CoverLetterSentence(text = editedText, factId = null)),
                isUserEdited = true,
            )
        }

        composeRule.setContent {
            TmrTheme {
                CoverLetterScreen(uiState = state, actions = provenanceNoActions())
            }
        }

        assertThat(state.editingOrdinal).isNull()
        assertThat(
            composeRule.onAllNodes(hasText("User-edited")).fetchSemanticsNodes(),
        ).isNotEmpty()
        assertThat(
            composeRule.onAllNodes(
                hasText("Your own words. TailorMyResume does not check hand edits.", substring = true),
            ).fetchSemanticsNodes(),
        ).isNotEmpty()
        assertThat(
            composeRule.onAllNodes(hasText("Still backed by your facts", substring = true)).fetchSemanticsNodes(),
        ).isEmpty()
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

private fun CoverLetterUiState.withProvenanceParagraph(
    change: (CoverLetterParagraph) -> CoverLetterParagraph,
): CoverLetterUiState = copy(
    paragraphs = paragraphs.map { paragraph ->
        if (paragraph.basis == CoverLetterBasis.CONFIRMED_FACT) change(paragraph) else paragraph
    },
)
