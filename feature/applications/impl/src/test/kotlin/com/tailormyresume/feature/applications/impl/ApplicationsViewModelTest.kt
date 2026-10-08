package com.tailormyresume.feature.applications.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.CreditKind
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.model.ExportRecord
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class ApplicationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository()
    private val sessionRepository = TestSessionRepository()
    private val paymentGateway = TestPaymentGateway().withFreeCredits(1)
    private val connectivityMonitor = TestConnectivityMonitor()
    private val exportHistoryRepository = TestExportHistoryRepository()
    private lateinit var viewModel: ApplicationsViewModel

    @Before
    fun setUp() {
        viewModel = ApplicationsViewModel(
            applicationRepository = applicationRepository,
            exportHistoryRepository = exportHistoryRepository,
            sessionRepository = sessionRepository,
            paymentGateway = paymentGateway,
            connectivityMonitor = connectivityMonitor,
        )
    }

    @Test
    fun uiState_beforeRepositoryEmits_isLoading() {
        assertThat(viewModel.uiState.value).isEqualTo(ApplicationsUiState.Loading())
    }

    @Test
    fun uiState_whenNoApplications_isEmpty() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(emptyList())
            runCurrent()

            assertThat(current()).isInstanceOf(ApplicationsUiState.Empty::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenApplicationsExist_sortsByUpdatedAtDescending() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(
                listOf(
                    testApplication(id = "oldest", updatedAtEpochSeconds = 100),
                    testApplication(id = "newest", updatedAtEpochSeconds = 300),
                    testApplication(id = "middle", updatedAtEpochSeconds = 200),
                ),
            )
            runCurrent()

            assertThat(current().ids()).containsExactly("newest", "middle", "oldest").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenApplicationIsUpdated_movesItToTheTop() = runTest {
        val first = testApplication(id = "first", updatedAtEpochSeconds = 100)
        val second = testApplication(id = "second", updatedAtEpochSeconds = 200)

        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(first, second))
            runCurrent()
            assertThat(current().ids()).containsExactly("second", "first").inOrder()

            applicationRepository.sendApplications(
                listOf(first.copy(updatedAt = Instant.fromEpochSeconds(500)), second),
            )
            runCurrent()

            assertThat(current().ids()).containsExactly("first", "second").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenAllApplicationsAreRemoved_isEmpty() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(testApplication("only")))
            runCurrent()
            applicationRepository.sendApplications(emptyList())
            runCurrent()

            assertThat(current()).isInstanceOf(ApplicationsUiState.Empty::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenApplicationsExist_exposesRowFactsFromTheStoredApplication() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(
                listOf(
                    testApplication(
                        id = "northwind",
                        updatedAtEpochSeconds = 1_000,
                        status = ApplicationStatus.APPLIED,
                        gapAnalysis = testGapAnalysis(),
                    ),
                ),
            )
            runCurrent()

            val row = current().rows().single()
            assertThat(row.id).isEqualTo("northwind")
            assertThat(row.role).isEqualTo("Role northwind")
            assertThat(row.company).isEqualTo("Company northwind")
            assertThat(row.status).isEqualTo(ApplicationStatus.APPLIED)
            assertThat(row.coverage.covered).isEqualTo(3)
            assertThat(row.coverage.total).isEqualTo(5)
            assertThat(row.isSyncPending).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenThereIsNoGapAnalysis_readsZeroOfZeroRatherThanNothing() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(testApplication("plain", updatedAtEpochSeconds = 10)))
            runCurrent()

            val row = current().rows().single()
            assertThat(row.coverage.covered).isEqualTo(0)
            assertThat(row.coverage.total).isEqualTo(0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_defaultScenario_isNotOfflineAndNothingIsPending() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(testApplication("only", updatedAtEpochSeconds = 10)))
            runCurrent()

            val state = current()
            assertThat(state.isOffline()).isFalse()
            assertThat(state.rows().single().isSyncPending).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_offlineScenario_showsTheOfflineBannerAndStillListsEveryRow() = runTest {
        viewModel.onEnter(DebugScenario.OFFLINE)

        viewModel.uiState.test {
            applicationRepository.sendApplications(
                listOf(
                    testApplication("one", updatedAtEpochSeconds = 100),
                    testApplication("two", updatedAtEpochSeconds = 200),
                ),
            )
            runCurrent()

            val state = current()
            assertThat(state.isOffline()).isTrue()
            assertThat(state.rows().map { row -> row.id }).containsExactly("two", "one")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_pendingScenario_marksTheNewestRowAndMovesItToTheTop() = runTest {
        viewModel.onEnter(DebugScenario.PENDING)

        viewModel.uiState.test {
            applicationRepository.sendApplications(
                listOf(
                    testApplication("older", updatedAtEpochSeconds = 100),
                    testApplication("newest", updatedAtEpochSeconds = 900),
                    testApplication("middle", updatedAtEpochSeconds = 500),
                ),
            )
            runCurrent()

            val rows = current().rows()
            assertThat(rows.map { row -> row.id }).containsExactly("newest", "middle", "older").inOrder()
            assertThat(rows.single { row -> row.isSyncPending }.id).isEqualTo("newest")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_header_greetsTheSignedInPersonByFirstName() = runTest {
        sessionRepository.sendAccount(SignInAccount.localAccount)

        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(testApplication("only")))
            runCurrent()

            assertThat(current().header.firstName).isEqualTo("Priya")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_header_withNoAccount_hasNoName() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(emptyList())
            runCurrent()

            assertThat(current().header.firstName).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_header_showsTheCreditsFromTheGateway() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(emptyList())
            runCurrent()

            assertThat(current().header.credits).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenTheDeviceGoesOffline_showsTheOfflineBanner() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(testApplication("only")))
            runCurrent()
            assertThat(current().isOffline()).isFalse()

            connectivityMonitor.setOnline(false)
            runCurrent()

            assertThat(current().isOffline()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uiState_whenOneApplicationHasExports_marksOnlyThatRowExported() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(
                listOf(
                    testApplication(id = "exported", updatedAtEpochSeconds = 200),
                    testApplication(id = "draft", updatedAtEpochSeconds = 100),
                ),
            )
            exportHistoryRepository.sendExports(
                listOf(
                    ExportRecord(
                        applicationId = "exported",
                        format = ExportFormat.PDF,
                        fileName = "cv.pdf",
                        exportedAt = Instant.fromEpochSeconds(300),
                        creditKind = CreditKind.FREE,
                    ),
                ),
            )
            runCurrent()

            val rows = current().rows()
            assertThat(rows.single { row -> row.id == "exported" }.isExported).isTrue()
            assertThat(rows.single { row -> row.id == "draft" }.isExported).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_statusChipChosen_opensTheStatusSheetWithTheStoredStatus() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(
                listOf(testApplication("northwind", status = ApplicationStatus.INTERVIEW)),
            )
            runCurrent()

            viewModel.onAction(ApplicationsAction.StatusChipChosen("northwind"))
            runCurrent()

            assertThat(current().statusSheet())
                .isEqualTo(ApplicationStatusSheetState("northwind", ApplicationStatus.INTERVIEW))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_statusChipChosen_forAnUnknownRow_leavesTheSheetClosed() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(testApplication("known")))
            runCurrent()

            viewModel.onAction(ApplicationsAction.StatusChipChosen("missing"))
            runCurrent()

            assertThat(current().statusSheet()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_statusSheetDismissed_closesTheSheet() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(testApplication("known")))
            runCurrent()
            viewModel.onAction(ApplicationsAction.StatusChipChosen("known"))
            runCurrent()

            viewModel.onAction(ApplicationsAction.StatusSheetDismissed)
            runCurrent()

            assertThat(current().statusSheet()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_statusChosen_writesTheStatusAndConfirmsWithUndo() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(testApplication("known")))
            runCurrent()
            viewModel.onAction(ApplicationsAction.StatusChipChosen("known"))
            runCurrent()

            viewModel.onAction(ApplicationsAction.StatusChosen(ApplicationStatus.OFFER))
            runCurrent()

            val state = current()
            assertThat(state.statusSheet()).isNull()
            assertThat(state.message())
                .isEqualTo(ApplicationStatusMessage(status = ApplicationStatus.OFFER, canUndo = true))
            assertThat(state.rows().single().status).isEqualTo(ApplicationStatus.OFFER)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_statusUndoChosen_restoresThePreviousStatusAndClearsTheMessage() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(testApplication("known")))
            runCurrent()
            viewModel.onAction(ApplicationsAction.StatusChipChosen("known"))
            viewModel.onAction(ApplicationsAction.StatusChosen(ApplicationStatus.OFFER))
            runCurrent()

            viewModel.onAction(ApplicationsAction.StatusUndoChosen)
            runCurrent()

            val state = current()
            assertThat(state.message()).isNull()
            assertThat(state.rows().single().status).isEqualTo(ApplicationStatus.SAVED)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_statusUndoChosen_withNothingToUndo_changesNothing() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(
                listOf(testApplication("known", status = ApplicationStatus.INTERVIEW)),
            )
            runCurrent()

            viewModel.onAction(ApplicationsAction.StatusUndoChosen)
            runCurrent()

            assertThat(current().rows().single().status).isEqualTo(ApplicationStatus.INTERVIEW)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_messageDismissed_clearsTheMessage() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(listOf(testApplication("known")))
            runCurrent()
            viewModel.onAction(ApplicationsAction.StatusChipChosen("known"))
            viewModel.onAction(ApplicationsAction.StatusChosen(ApplicationStatus.OFFER))
            runCurrent()

            viewModel.onAction(ApplicationsAction.MessageDismissed)
            runCurrent()

            assertThat(current().message()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_statusChipChosen_replacesAnOpenSheet() = runTest {
        viewModel.uiState.test {
            applicationRepository.sendApplications(
                listOf(
                    testApplication("one", updatedAtEpochSeconds = 100, status = ApplicationStatus.SAVED),
                    testApplication("two", updatedAtEpochSeconds = 200, status = ApplicationStatus.OFFER),
                ),
            )
            runCurrent()

            viewModel.onAction(ApplicationsAction.StatusChipChosen("one"))
            runCurrent()
            viewModel.onAction(ApplicationsAction.StatusChipChosen("two"))
            runCurrent()

            assertThat(current().statusSheet()?.rowId).isEqualTo("two")
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun current(): ApplicationsUiState = viewModel.uiState.value

    private fun ApplicationsUiState.ids(): List<String> = rows().map { row -> row.id }

    private fun ApplicationsUiState.rows(): List<ApplicationListRow> =
        (this as ApplicationsUiState.Applications).rows

    private fun ApplicationsUiState.isOffline(): Boolean =
        (this as ApplicationsUiState.Applications).isOffline

    private fun ApplicationsUiState.statusSheet(): ApplicationStatusSheetState? =
        (this as ApplicationsUiState.Applications).statusSheet

    private fun ApplicationsUiState.message(): ApplicationStatusMessage? =
        (this as ApplicationsUiState.Applications).message
}
