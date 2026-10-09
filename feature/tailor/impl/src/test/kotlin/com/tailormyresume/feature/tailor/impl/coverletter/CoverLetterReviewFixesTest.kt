package com.tailormyresume.feature.tailor.impl.coverletter

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.coverletter.CoverLetterComposer
import com.tailormyresume.core.domain.coverletter.CoverLetterDraft
import com.tailormyresume.core.domain.coverletter.CoverLetterSource
import com.tailormyresume.core.domain.coverletter.GenerateCoverLetterUseCase
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.JobDescription
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
class CoverLetterReviewFixesTest {

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
    fun generatingFactCountIsWhatTheLetterWillCite() = runTest {
        given()
        val stalled = object : CoverLetterSource {
            override suspend fun invoke(
                candidate: CandidateProfile,
                job: JobDescription,
                analysis: JobAnalysisResult,
                maxEvidence: Int,
            ): CoverLetterDraft = CompletableDeferred<CoverLetterDraft>().await()
        }
        val generating = newViewModel(stalled)
        generating.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        generating.onAction(CoverLetterAction.WriteOne)
        val shown = generating.uiState.value.factCount

        val written = newViewModel(GenerateCoverLetterUseCase())
        written.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        written.onAction(CoverLetterAction.WriteOne)

        assertThat(shown).isEqualTo(written.uiState.value.factCount)
        assertThat(shown).isAtMost(CoverLetterComposer.DEFAULT_MAX_EVIDENCE)
        assertThat(shown).isGreaterThan(0)
    }

    @Test
    fun errorWhileOfflineShowsNoSavedOnThisPhoneBanner() {
        show(CoverLetterUiState(stage = CoverLetterStage.ERROR, isOffline = true))

        composeRule
            .onAllNodesWithText("You're offline. This letter is saved on this phone.")
            .assertCountEquals(0)
    }

    @Test
    fun onlyReadyAndNoMatchingEvidenceShowTheOfflineBanner() {
        val showing = CoverLetterStage.entries.filter { stage ->
            CoverLetterUiState(stage = stage, isOffline = true).showsOfflineBanner
        }

        assertThat(showing).containsExactly(CoverLetterStage.READY, CoverLetterStage.NO_MATCHING_EVIDENCE)
    }
}

private const val APPLICATION_ID = "application-northwind-1"
