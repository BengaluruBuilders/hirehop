package com.tailormyresume.feature.tailor.impl.coverletter

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.JobAnalysisResult
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

private const val APPLICATION_ID = "application-northwind-1"

private class StalledSource(override val choosesEvidence: Boolean) : CoverLetterSource {
    override suspend fun invoke(
        candidate: CandidateProfile,
        job: JobDescription,
        analysis: JobAnalysisResult,
        maxEvidence: Int,
    ): CoverLetterDraft = CompletableDeferred<CoverLetterDraft>().await()
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class CoverLetterFactCountTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeRule = createComposeRule()

    private fun generating(source: CoverLetterSource): CoverLetterUiState {
        val applications = TestApplicationRepository().apply { sendApplications(listOf(canonicalApplication)) }
        val profiles = TestProfileRepository().apply { sendProfile(canonicalCandidateProfile) }
        val subject = CoverLetterViewModel(
            applicationRepository = applications,
            profileRepository = profiles,
            generateCoverLetter = source,
            connectivityMonitor = TestConnectivityMonitor(),
            contentReportRepository = TestContentReportRepository(),
            coverLetterRepository = TestCoverLetterRepository(),
            exportHistoryRepository = TestExportHistoryRepository(),
            clock = TestClock(),
        )
        subject.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        subject.onAction(CoverLetterAction.WriteOne)
        return subject.uiState.value
    }

    @Test
    fun aSourceThatChoosesItsOwnEvidenceHidesTheFactCount() = runTest {
        assertThat(generating(StalledSource(choosesEvidence = false)).showsFactCount).isFalse()
    }

    @Test
    fun aSourceThatFollowsTheCapKeepsTheFactCount() = runTest {
        assertThat(generating(StalledSource(choosesEvidence = true)).showsFactCount).isTrue()
        assertThat(generating(GenerateCoverLetterUseCase()).showsFactCount).isTrue()
    }

    @Test
    fun theGeneratingCardShowsTheFactCountByDefault() {
        showGenerating(showsFactCount = true)

        composeRule.onAllNodes(hasContentDescription("7 facts", substring = true)).assertCountEquals(1)
    }

    @Test
    fun theGeneratingCardShowsNoFactCountWhenItIsHidden() {
        showGenerating(showsFactCount = false)

        composeRule.onAllNodes(hasContentDescription("7 facts", substring = true)).assertCountEquals(0)
    }

    private fun showGenerating(showsFactCount: Boolean) {
        composeRule.setContent {
            TmrTheme {
                CoverLetterScreen(
                    uiState = CoverLetterUiState(
                        stage = CoverLetterStage.GENERATING,
                        factCount = 7,
                        showsFactCount = showsFactCount,
                    ),
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
}
