package com.tailormyresume.feature.applications.impl

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.prep.RequirementPhrase
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.repository.TestContentReportRepository
import com.tailormyresume.core.testing.repository.TestCoverLetterRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestPrepPlanRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ApplicationDetailRequirementSheetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = RecordingApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val exportHistoryRepository = TestExportHistoryRepository()
    private val paymentGateway = TestPaymentGateway().withFreeCredits(1)
    private val connectivityMonitor = TestConnectivityMonitor()
    private val prepPlanRepository = TestPrepPlanRepository()
    private val contentReportRepository = TestContentReportRepository()
    private val coverLetterRepository = TestCoverLetterRepository()
    private val application = testApplication(
        id = APPLICATION_ID,
        gapAnalysis = testGapAnalysis(),
        tailoredResume = testTailoredResume(),
    )
    private val applicationScope = CoroutineScope(UnconfinedTestDispatcher())
    private val viewModelStore = ViewModelStore()
    private lateinit var viewModel: ApplicationDetailViewModel

    @Before
    fun setUp() {
        viewModel = ViewModelProvider.create(
            store = viewModelStore,
            factory = viewModelFactory {
                initializer {
                    ApplicationDetailViewModel(
                        applicationRepository = applicationRepository,
                        profileRepository = profileRepository,
                        exportHistoryRepository = exportHistoryRepository,
                        prepPlanRepository = prepPlanRepository,
                        contentReportRepository = contentReportRepository,
                        coverLetterRepository = coverLetterRepository,
                        clock = TestClock(),
                        paymentGateway = paymentGateway,
                        connectivityMonitor = connectivityMonitor,
                        applicationScope = applicationScope,
                        applicationId = APPLICATION_ID,
                    )
                }
            },
        )[ApplicationDetailViewModel::class]
    }

    @After
    fun tearDown() {
        applicationScope.cancel()
    }

    @Test
    fun onAction_requirementChosen_opensTheSheetWithTheRequirementTextAndStatus() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.RequirementChosen("d"))

            assertThat(current().ready().requirementSheet).isEqualTo(
                WorkspaceRequirementSheetState(
                    id = "d",
                    name = RequirementPhrase.of("Requirement d"),
                    requirementText = "Requirement d",
                    status = MatchStatus.GAP,
                    evidenceIds = emptyList(),
                    isInPrepPlan = false,
                ),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_requirementSheet_carriesTheStoredEvidenceIdsOfAMetRow() = runTest {
        val withEvidence = GapAnalysis(
            matches = listOf(
                testMatch("a", MatchStatus.MET, RequirementPriority.MUST_HAVE)
                    .copy(evidenceIds = listOf("S-01", "W-02")),
            ),
            keywordCoverage = KeywordCoverage(covered = 1, total = 1),
        )

        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application.copy(gapAnalysis = withEvidence)))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.RequirementChosen("a"))

            val state = current().ready()
            assertThat(state.requirementSheet?.evidenceIds).containsExactly("S-01", "W-02").inOrder()
            assertThat(state.matches.single { match -> match.id == "a" }.evidenceIds)
                .containsExactly("S-01", "W-02").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_requirementChosen_thenDismissed_closesTheSheet() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.RequirementChosen("d"))
            assertThat(current().ready().requirementSheet).isNotNull()

            viewModel.onAction(ApplicationWorkspaceAction.RequirementDismissed)

            assertThat(current().ready().requirementSheet).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_requirementChosen_withAnUnknownId_opensNoSheet() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.RequirementChosen("z"))

            assertThat(current().ready().requirementSheet).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_requirementPrepAdded_addsOnePrepTaskForThatApplication() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()
            viewModel.onAction(ApplicationWorkspaceAction.RequirementChosen("d"))

            viewModel.onAction(ApplicationWorkspaceAction.RequirementPrepAddChosen("d"))
            runCurrent()

            val state = current().ready()
            val task = state.prepTasks.single()
            assertThat(task.id).isEqualTo("d")
            assertThat(task.requirementText).isEqualTo("Requirement d")
            assertThat(state.requirementSheet?.id).isEqualTo("d")
            assertThat(state.requirementSheet?.isInPrepPlan).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_requirementPrepAdded_twice_addsNoDuplicate() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.RequirementPrepAddChosen("d"))
            runCurrent()
            viewModel.onAction(ApplicationWorkspaceAction.RequirementPrepAddChosen("d"))
            runCurrent()

            assertThat(current().ready().prepTasks).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_requirementPrepAdded_onAMetRow_addsNothing() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.RequirementPrepAddChosen("a"))
            runCurrent()

            assertThat(current().ready().prepTasks).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_requirementPrepAdded_onAPartialRow_addsOneTask() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(application))
            runCurrent()

            viewModel.onAction(ApplicationWorkspaceAction.RequirementPrepAddChosen("c"))
            runCurrent()

            assertThat(current().ready().prepTasks).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun current(): ApplicationDetailUiState = viewModel.uiState.value

    private fun ApplicationDetailUiState.ready(): ApplicationDetailUiState.Ready =
        this as ApplicationDetailUiState.Ready

    private companion object {
        const val APPLICATION_ID = "application-1"
    }
}
