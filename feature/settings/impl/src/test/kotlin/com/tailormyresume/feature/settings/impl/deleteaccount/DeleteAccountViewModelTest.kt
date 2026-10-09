package com.tailormyresume.feature.settings.impl.deleteaccount

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.mock.NoMockLatency
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.account.AccountCreditBalance
import com.tailormyresume.core.domain.account.AccountDeletionStep
import com.tailormyresume.core.domain.account.DeleteAccountUseCase
import com.tailormyresume.core.domain.offline.OfflineServerAccountDeleter
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.navigation.PendingNavigation
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.data.sampleApplication
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.gateway.TestSignInGateway
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.settings.api.navigation.AccountDeletedNavKey
import com.tailormyresume.feature.settings.api.navigation.DeleteAccountNavKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteAccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applications = MutableStateFlow<List<JobApplication>>(FOUR_APPLICATIONS)
    private val profile = MutableStateFlow<CandidateProfile?>(canonicalCandidateProfile)
    private val gate = CompletableDeferred<Unit>()

    @Before
    fun clearPendingNavigation() {
        PendingNavigation.consume()
    }

    @After
    fun dropPendingNavigation() {
        PendingNavigation.consume()
    }

    private val connectivity = TestConnectivityMonitor()
    private val sessionRepository = TestSessionRepository().apply { sendAccount(SignInAccount.localAccount) }
    private var shouldFail = false
    private var waitsForGate = false
    private var applicationDeleteCalls = 0

    @Test
    fun ready_countsTheRealData() = runTest {
        val viewModel = enteredViewModel()

        val ready = viewModel.ready()
        assertThat(ready.counts.profileFacts).isEqualTo(27)
        assertThat(ready.counts.applications).isEqualTo(4)
        assertThat(ready.counts.unusedCredits).isEqualTo(4)
        assertThat(ready.accountEmail).isEqualTo(SignInAccount.localAccount.email)
        assertThat(ready.isOffline).isFalse()
        assertThat(ready.failure).isNull()
    }

    @Test
    fun tappingDeleteOnlyAsksToConfirmAndDeletesNothing() = runTest {
        val viewModel = enteredViewModel()

        viewModel.onDeleteTapped()

        assertThat(viewModel.ready().isConfirmVisible).isTrue()
        assertThat(applications.value).hasSize(4)
        assertThat(profile.value).isEqualTo(canonicalCandidateProfile)
        assertThat(PendingNavigation.consume()).isEmpty()
    }

    @Test
    fun cancelClosesTheConfirmAndDeletesNothing() = runTest {
        val viewModel = enteredViewModel()
        viewModel.onDeleteTapped()

        viewModel.onDeleteDismissed()

        assertThat(viewModel.ready().isConfirmVisible).isFalse()
        assertThat(applications.value).hasSize(4)
        assertThat(profile.value).isEqualTo(canonicalCandidateProfile)
        assertThat(PendingNavigation.consume()).isEmpty()
    }

    @Test
    fun confirmWithoutTheDialogDoesNothing() = runTest {
        val viewModel = enteredViewModel()

        viewModel.onDeleteConfirmed()

        assertThat(viewModel.ready().isConfirmVisible).isFalse()
        assertThat(applications.value).hasSize(4)
        assertThat(profile.value).isEqualTo(canonicalCandidateProfile)
        assertThat(PendingNavigation.consume()).isEmpty()
    }

    @Test
    fun confirmingStartsTheDeletionExactlyOnce() = runTest {
        waitsForGate = true
        val viewModel = enteredViewModel()
        viewModel.onDeleteTapped()
        assertThat(applicationDeleteCalls).isEqualTo(0)

        viewModel.onDeleteConfirmed()
        viewModel.onDeleteConfirmed()

        assertThat(viewModel.uiState.value).isInstanceOf(DeleteAccountUiState.Deleting::class.java)
        assertThat(applicationDeleteCalls).isEqualTo(1)
        assertThat(PendingNavigation.consume()).containsExactly(AccountDeletedNavKey)
        gate.complete(Unit)
    }

    @Test
    fun deleting_namesTheStepsInTheOrderTheUseCaseRunsThem() = runTest {
        waitsForGate = true
        val viewModel = enteredViewModel()

        viewModel.onDeleteTapped()
        viewModel.onDeleteConfirmed()

        val deleting = viewModel.uiState.value as DeleteAccountUiState.Deleting
        assertThat(deleting.step).isEqualTo(AccountDeletionStep.DELETING_APPLICATIONS)
        assertThat(deleting.counts.applications).isEqualTo(4)
        assertThat(profile.value).isNull()
        gate.complete(Unit)
    }

    @Test
    fun done_meansTheDataIsGoneAndTheSessionIsClearedAndTheDoneScreenWaitsForTheNewRoot() = runTest {
        val viewModel = enteredViewModel()

        viewModel.onDeleteTapped()
        viewModel.onDeleteConfirmed()

        assertThat(PendingNavigation.consume()).containsExactly(AccountDeletedNavKey)
        assertThat(applications.value).isEmpty()
        assertThat(profile.value).isNull()
        assertThat(sessionRepository.observeAccount().first()).isNull()
    }

    @Test
    fun error_saysTheDataIsStillThereBecauseItIs() = runTest {
        shouldFail = true
        val viewModel = enteredViewModel()

        viewModel.onDeleteTapped()
        viewModel.onDeleteConfirmed()

        val ready = viewModel.ready()
        assertThat(ready.failure).isEqualTo(DeleteAccountFailure.DATA_INTACT)
        assertThat(ready.isConfirmVisible).isFalse()
        assertThat(PendingNavigation.consume()).isEmpty()
        assertThat(applications.value.map { application -> application.id })
            .containsExactlyElementsIn(FOUR_APPLICATIONS.map { application -> application.id })
        assertThat(profile.value).isEqualTo(canonicalCandidateProfile)
        assertThat(sessionRepository.observeAccount().first()).isEqualTo(SignInAccount.localAccount)
    }

    @Test
    fun offline_blocksTheDeletion() = runTest {
        connectivity.setOnline(false)
        val viewModel = enteredViewModel()

        viewModel.onDeleteTapped()
        viewModel.onDeleteConfirmed()

        val ready = viewModel.ready()
        assertThat(ready.isOffline).isTrue()
        assertThat(ready.isConfirmVisible).isFalse()
        assertThat(applications.value).isNotEmpty()
        assertThat(PendingNavigation.consume()).isEmpty()
    }

    @Test
    fun onEnter_withTheOfflineScenario_blocksTheDeletion() = runTest {
        val viewModel = enteredViewModel(DebugScenario.OFFLINE)

        viewModel.onDeleteTapped()
        viewModel.onDeleteConfirmed()

        val ready = viewModel.ready()
        assertThat(ready.isOffline).isTrue()
        assertThat(ready.isConfirmVisible).isFalse()
        assertThat(applications.value).isNotEmpty()
        assertThat(PendingNavigation.consume()).isEmpty()
    }

    @Test
    fun onEnter_withTheDeletingScenario_showsFactsDoneAndApplicationsInProgress() = runTest {
        val viewModel = enteredViewModel(DebugScenario.DELETING)

        val deleting = viewModel.uiState.value as DeleteAccountUiState.Deleting
        assertThat(deleting.step).isEqualTo(AccountDeletionStep.DELETING_APPLICATIONS)
        assertThat(applications.value).isNotEmpty()
    }

    @Test
    fun onEnter_withTheErrorScenario_showsTheErrorState() = runTest {
        val viewModel = enteredViewModel(DebugScenario.ERROR)

        assertThat(viewModel.ready().failure).isEqualTo(DeleteAccountFailure.DATA_INTACT)
    }

    @Test
    fun onEnter_twice_keepsTheFirstCounts() = runTest {
        val viewModel = enteredViewModel()

        applications.value = emptyList()
        viewModel.onEnter(DeleteAccountNavKey())

        assertThat(viewModel.ready().counts.applications).isEqualTo(4)
    }

    private fun TestScope.enteredViewModel(scenario: DebugScenario = DebugScenario.DEFAULT): DeleteAccountViewModel {
        val viewModel = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        viewModel.onEnter(DeleteAccountNavKey(scenario = scenario))
        return viewModel
    }

    private fun DeleteAccountViewModel.ready(): DeleteAccountUiState.Ready =
        uiState.value as DeleteAccountUiState.Ready

    private fun viewModel(): DeleteAccountViewModel = DeleteAccountViewModel(
        connectivityMonitor = connectivity,
        sessionRepository = sessionRepository,
        deleteAccount = DeleteAccountUseCase(
            applicationRepository = GateApplicationRepository(
                applications = applications,
                gate = gate,
                waits = { waitsForGate },
                shouldFail = { shouldFail },
                onDelete = { applicationDeleteCalls++ },
            ),
            profileRepository = StaticProfileRepository(profile = profile),
            exportHistoryRepository = TestExportHistoryRepository(),
            sessionRepository = sessionRepository,
            signInGateway = TestSignInGateway(sessionRepository),
            serverAccountDeleter = OfflineServerAccountDeleter(),
            creditBalance = AccountCreditBalance(
                paymentGateway = TestPaymentGateway().withFreeCredits(4),
            ),
            latency = NoMockLatency,
        ),
    )

    private class GateApplicationRepository(
        private val applications: MutableStateFlow<List<JobApplication>>,
        private val gate: CompletableDeferred<Unit>,
        private val waits: () -> Boolean,
        private val shouldFail: () -> Boolean,
        private val onDelete: () -> Unit,
    ) : ApplicationRepository {

        override fun observeApplications(): Flow<List<JobApplication>> = applications

        override fun observeApplication(id: String): Flow<JobApplication?> = MutableStateFlow(null)

        override suspend fun upsertApplication(application: JobApplication) {
            applications.update { current ->
                if (current.any { it.id == application.id }) {
                    current.map { if (it.id == application.id) application else it }
                } else {
                    current + application
                }
            }
        }

        override suspend fun updateStatus(id: String, status: ApplicationStatus) = Unit

        override suspend fun updateNotes(id: String, notes: String) = Unit

        override suspend fun deleteApplication(id: String) {
            onDelete()
            if (waits()) gate.await()
            if (shouldFail()) throw IllegalStateException("delete failed for $id")
            applications.update { current -> current.filterNot { it.id == id } }
        }
    }

    private class StaticProfileRepository(
        private val profile: MutableStateFlow<CandidateProfile?>,
    ) : ProfileRepository {

        override fun observeProfile(): Flow<CandidateProfile?> = profile

        override suspend fun saveProfile(profile: CandidateProfile) {
            this.profile.value = profile
        }

        override suspend fun clearProfile() {
            profile.value = null
        }
    }

    private companion object {
        val FOUR_APPLICATIONS = listOf("1", "2", "3", "4").map { index ->
            sampleApplication.copy(id = "application-$index", status = ApplicationStatus.SAVED)
        }
    }
}
