package com.tailormyresume.feature.settings.impl.deleteaccount

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.mock.NoMockLatency
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.domain.PurchaseEntitlement
import com.tailormyresume.core.domain.account.AccountCreditBalance
import com.tailormyresume.core.domain.account.DeleteAccountUseCase
import com.tailormyresume.core.domain.offline.OfflineServerAccountDeleter
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.navigation.PendingNavigation
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.data.sampleApplication
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.gateway.TestSignInGateway
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.settings.api.navigation.DeleteAccountNavKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteAccountFirstReadyTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
    private val applicationRepository = TestApplicationRepository().apply { sendApplications(FOUR_APPLICATIONS) }
    private val profileRepository = TestProfileRepository().apply { sendProfile(canonicalCandidateProfile) }
    private val entitlementDeferred = CompletableDeferred<PurchaseEntitlement>()
    private var entitlementFails = false
    private var perCall: (() -> CompletableDeferred<PurchaseEntitlement>)? = null
    private var cachedCredits = 4

    @Test
    fun entitlementSuspended_stillReachesReadyWithLocalCountsAndCachedCredits() = runTest {
        val viewModel = enteredViewModel()

        val ready = viewModel.ready()
        assertThat(ready.counts.applications).isEqualTo(4)
        assertThat(ready.counts.unusedCredits).isEqualTo(4)
        entitlementDeferred.complete(TestPaymentGateway().withFreeCredits(4).entitlement())
    }

    @Test
    fun refreshArriving_updatesTheCreditCount() = runTest {
        val refreshed = TestPaymentGateway().withFreeCredits(9).entitlement()
        val viewModel = enteredViewModel()

        entitlementDeferred.complete(refreshed)

        assertThat(viewModel.ready().counts.unusedCredits).isEqualTo(9)
    }

    @Test
    fun refreshFailing_keepsTheCachedCount() = runTest {
        entitlementFails = true
        val viewModel = enteredViewModel()

        assertThat(viewModel.ready().counts.unusedCredits).isEqualTo(4)
    }

    @Test
    fun tappingDelete_callsTheWalletOnceMoreNotTwice() = runTest {
        val calls = mutableListOf<CompletableDeferred<PurchaseEntitlement>>()
        perCall = { CompletableDeferred<PurchaseEntitlement>().also { calls += it } }
        val viewModel = enteredViewModel()
        viewModel.ready()

        viewModel.onDeleteTapped()
        viewModel.onDeleteTapped()
        calls.forEach { it.complete(TestPaymentGateway().withFreeCredits(9).entitlement()) }

        assertThat(viewModel.ready().isConfirmVisible).isTrue()
        assertThat(calls).hasSize(2)
    }

    @Test
    fun tappingDelete_showsTheFreshCreditCountNotTheCache() = runTest {
        val answers = ArrayDeque(listOf(9, 7).map { CompletableDeferred(TestPaymentGateway().withFreeCredits(it).entitlement()) })
        perCall = { answers.removeFirst() }
        cachedCredits = 0
        val viewModel = enteredViewModel()
        viewModel.ready()

        viewModel.onDeleteTapped()

        assertThat(viewModel.ready().isConfirmVisible).isTrue()
        assertThat(viewModel.ready().counts.unusedCredits).isEqualTo(7)
    }

    @Test
    fun tappingDelete_cancelsASlowEnterRefreshSoItCannotOverwriteTheDeletingCounts() = runTest {
        val calls = listOf(
            CompletableDeferred<PurchaseEntitlement>(),
            CompletableDeferred<PurchaseEntitlement>(),
            CompletableDeferred(TestPaymentGateway().withFreeCredits(9).entitlement()),
        )
        var next = 0
        perCall = { calls[next++] }
        val viewModel = enteredViewModel()

        viewModel.onDeleteTapped()
        calls[1].complete(TestPaymentGateway().withFreeCredits(9).entitlement())
        viewModel.onDeleteConfirmed()
        calls[0].complete(TestPaymentGateway().withFreeCredits(9).entitlement())

        val deleting = viewModel.uiState.value as DeleteAccountUiState.Deleting
        assertThat(deleting.counts.applications).isEqualTo(4)
    }

    private fun TestScope.enteredViewModel(): DeleteAccountViewModel {
        val viewModel = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        viewModel.onEnter(DeleteAccountNavKey())
        return viewModel
    }

    private fun DeleteAccountViewModel.ready(): DeleteAccountUiState.Ready =
        uiState.value as DeleteAccountUiState.Ready

    private fun viewModel(): DeleteAccountViewModel = DeleteAccountViewModel(
        connectivityMonitor = connectivity,
        sessionRepository = sessionRepository,
        deleteAccount = DeleteAccountUseCase(
            applicationRepository = applicationRepository,
            profileRepository = profileRepository,
            exportHistoryRepository = TestExportHistoryRepository(),
            sessionRepository = sessionRepository,
            signInGateway = TestSignInGateway(sessionRepository),
            serverAccountDeleter = OfflineServerAccountDeleter(),
            creditBalance = AccountCreditBalance(
                paymentGateway = DeferredEntitlementPaymentGateway(
                    delegate = TestPaymentGateway().withFreeCredits(cachedCredits),
                    entitlement = entitlementDeferred,
                    fails = { entitlementFails },
                    perCall = { perCall },
                ),
            ),
            latency = NoMockLatency,
        ),
    )

    private class DeferredEntitlementPaymentGateway(
        private val delegate: PaymentGateway,
        private val entitlement: CompletableDeferred<PurchaseEntitlement>,
        private val fails: () -> Boolean,
        private val perCall: () -> (() -> CompletableDeferred<PurchaseEntitlement>)?,
    ) : PaymentGateway by delegate {

        override suspend fun entitlement(): PurchaseEntitlement {
            if (fails()) throw IOException("entitlement unavailable")
            return (perCall()?.invoke() ?: entitlement).await()
        }
    }

    private companion object {
        val FOUR_APPLICATIONS = listOf("1", "2", "3", "4").map { index ->
            sampleApplication.copy(id = "application-$index", status = ApplicationStatus.SAVED)
        }
    }
}
