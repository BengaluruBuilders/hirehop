package com.tailormyresume.feature.tailor.impl.coverletter

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.coverletter.CoverLetterDraft
import com.tailormyresume.core.domain.coverletter.CoverLetterSource
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.factCounts
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestContentReportRepository
import com.tailormyresume.core.testing.repository.TestCoverLetterRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.tailor.api.navigation.CoverLetterNavKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class CoverLetterBatchTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeRule = createComposeRule()

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val connectivity = TestConnectivityMonitor()
    private val reports = TestContentReportRepository()
    private val coverLetters = TestCoverLetterRepository()
    private val exportHistory = TestExportHistoryRepository()
    private val clock = TestClock()

    private fun newViewModel(generateCoverLetter: CoverLetterSource): CoverLetterViewModel = CoverLetterViewModel(
        applicationRepository = applicationRepository,
        profileRepository = profileRepository,
        generateCoverLetter = generateCoverLetter,
        connectivityMonitor = connectivity,
        contentReportRepository = reports,
        coverLetterRepository = coverLetters,
        exportHistoryRepository = exportHistory,
        clock = clock,
    )

    private fun show(uiState: CoverLetterUiState) {
        composeRule.setContent {
            TmrTheme {
                CoverLetterScreen(
                    uiState = uiState,
                    actions = CoverLetterActions(
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
                    ),
                )
            }
        }
    }

    private fun given() {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalCandidateProfile)
    }

    @Test
    fun generatingStateCarriesTheConfirmedFactCount() = runTest {
        given()
        val never = CompletableDeferred<CoverLetterDraft>()
        val stalledSource = object : CoverLetterSource {
            override suspend fun invoke(
                candidate: CandidateProfile,
                job: JobDescription,
                analysis: JobAnalysisResult,
                maxEvidence: Int,
            ): CoverLetterDraft = never.await()
        }
        val viewModel = newViewModel(stalledSource)

        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        viewModel.onAction(CoverLetterAction.WriteOne)

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(CoverLetterStage.GENERATING)
        assertThat(state.factCount).isEqualTo(canonicalCandidateProfile.factCounts().confirmed)
        assertThat(state.factCount).isGreaterThan(0)
    }

    @Test
    fun offerStageShowsNoOfflineBanner() {
        show(
            CoverLetterUiState(
                stage = CoverLetterStage.OFFER,
                jobTitle = "Associate Analyst",
                jobCompany = "Northwind GCC",
                isOffline = true,
            ),
        )

        composeRule
            .onAllNodesWithText("You're offline. This letter is saved on this phone.")
            .assertCountEquals(0)
        composeRule.onNodeWithText("Write one").assertIsDisplayed()
    }

    @Test
    fun generatingShowsWritingThreeParagraphsAndTheFactCount() {
        show(CoverLetterUiState(stage = CoverLetterStage.GENERATING, factCount = 6))

        composeRule.onNodeWithContentDescription("Writing 3 paragraphs", substring = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("6 facts", substring = true).assertIsDisplayed()
    }
}

private const val APPLICATION_ID = "application-northwind-1"
