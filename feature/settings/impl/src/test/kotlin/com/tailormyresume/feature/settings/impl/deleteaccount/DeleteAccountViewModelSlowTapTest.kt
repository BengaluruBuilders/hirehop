package com.tailormyresume.feature.settings.impl.deleteaccount

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.mock.NoMockLatency
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.domain.PurchaseEntitlement
import com.tailormyresume.core.domain.account.AccountCreditBalance
import com.tailormyresume.core.domain.account.DeleteAccountUseCase
import com.tailormyresume.core.domain.offline.OfflineServerAccountDeleter
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.CandidateProfile
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
import com.tailormyresume.feature.settings.api.navigation.DeleteAccountNavKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteAccountViewModelSlowTapTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applications = MutableStateFlow(FOUR_APPLICATIONS)
    private val profile = MutableStateFlow<CandidateProfile?>(canonicalCandidateProfile)
    private val connectivity = TestConnectivityMonitor()
    private val sessionRepository = TestSessionRepository().apply { sendAccount(SignInAccount.localAccount) }
    private val entitlementCalls = mutableListOf<CompletableDeferred<Unit>>()
    private var slowEntitlement = false

    @Before
    fun clearPendingNavigation() {
        PendingNavigation.consume()
    }

    @After
    fun dropPendingNavigation() {
        PendingNavigation.consume()
    }

    @Test
    fun tappingTwiceWhileTheRefreshRunsStartsOnlyOneRefresh() = runTest {
        val viewModel = enteredViewModel()
        slowEntitlement = true

        viewModel.onDeleteTapped()
        viewModel.onDeleteTapped()

        assertThat(entitlementCalls).hasSize(1)
    }

    @Test
    fun aSecondTapDoesNotReopenTheDialogAfterCancel() = runTest {
        val viewModel = enteredViewModel()
        slowEntitlement = true

        viewModel.onDeleteTapped()
        viewModel.onDeleteTapped()
        entitlementCalls.first().complete(Unit)
        assertThat(viewModel.ready().isConfirmVisible).isTrue()
        viewModel.onDeleteDismissed()
        entitlementCalls.drop(1).forEach { it.complete(Unit) }

        assertThat(viewModel.ready().isConfirmVisible).isFalse()
    }

    @Test
    fun aRefreshThatFinishesAfterGoingOfflineShowsNoDialogAndWritesNoCounts() = runTest {
        val viewModel = enteredViewModel()
        slowEntitlement = true

        viewModel.onDeleteTapped()
        connectivity.setOnline(false)
        applications.value = FOUR_APPLICATIONS.drop(1)
        entitlementCalls.single().complete(Unit)

        val ready = viewModel.ready()
        assertThat(ready.isConfirmVisible).isFalse()
        assertThat(ready.counts.applications).isEqualTo(4)
    }

    @Test
    fun aTapIsAcceptedAgainOnceTheRefreshHasFinished() = runTest {
        val viewModel = enteredViewModel()
        slowEntitlement = true
        viewModel.onDeleteTapped()
        entitlementCalls.single().complete(Unit)
        viewModel.onDeleteDismissed()

        viewModel.onDeleteTapped()
        entitlementCalls.last().complete(Unit)

        assertThat(entitlementCalls).hasSize(2)
        assertThat(viewModel.ready().isConfirmVisible).isTrue()
    }

    private fun TestScope.enteredViewModel(): DeleteAccountViewModel {
        val viewModel = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        viewModel.onEnter(DeleteAccountNavKey())
        return viewModel
    }

    private fun DeleteAccountViewModel.ready(): DeleteAccountUiState.Ready = uiState.value as DeleteAccountUiState.Ready

    private fun viewModel(): DeleteAccountViewModel {
        val delegate = TestPaymentGateway().withFreeCredits(4)
        val gateway = object : PaymentGateway by delegate {
            override suspend fun entitlement(): PurchaseEntitlement {
                if (slowEntitlement) CompletableDeferred<Unit>().also { entitlementCalls += it }.await()
                return delegate.entitlement()
            }
        }
        return DeleteAccountViewModel(
            connectivityMonitor = connectivity,
            sessionRepository = sessionRepository,
            deleteAccount = DeleteAccountUseCase(
                applicationRepository = ListApplications(applications),
                profileRepository = StaticProfile(profile),
                exportHistoryRepository = TestExportHistoryRepository(),
                sessionRepository = sessionRepository,
                signInGateway = TestSignInGateway(sessionRepository),
                serverAccountDeleter = OfflineServerAccountDeleter(),
                creditBalance = AccountCreditBalance(paymentGateway = gateway),
                latency = NoMockLatency,
            ),
        )
    }

    private class ListApplications(
        private val applications: MutableStateFlow<List<JobApplication>>,
    ) : ApplicationRepository {
        override fun observeApplications(): Flow<List<JobApplication>> = applications
        override fun observeApplication(id: String): Flow<JobApplication?> = MutableStateFlow(null)
        override suspend fun upsertApplication(application: JobApplication) = Unit
        override suspend fun updateStatus(id: String, status: ApplicationStatus) = Unit
        override suspend fun updateNotes(id: String, notes: String) = Unit
        override suspend fun deleteApplication(id: String) = Unit
    }

    private class StaticProfile(
        private val profile: MutableStateFlow<CandidateProfile?>,
    ) : ProfileRepository {
        override fun observeProfile(): Flow<CandidateProfile?> = profile
        override suspend fun saveProfile(profile: CandidateProfile) = Unit
        override suspend fun clearProfile() = Unit
    }

    private companion object {
        val FOUR_APPLICATIONS = listOf("1", "2", "3", "4").map { index ->
            sampleApplication.copy(id = "application-$index", status = ApplicationStatus.SAVED)
        }
    }
}
