package com.tailormyresume.feature.settings.impl.deleteaccount

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.mock.NoMockLatency
import com.tailormyresume.core.data.repository.PendingWipeState
import com.tailormyresume.core.domain.SignInGateway
import com.tailormyresume.core.domain.account.AccountCreditBalance
import com.tailormyresume.core.domain.account.AccountWipeFinisher
import com.tailormyresume.core.domain.account.DeleteAccountUseCase
import com.tailormyresume.core.domain.account.FinishPendingAccountWipeUseCase
import com.tailormyresume.core.domain.account.ServerAccountDeleter
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.navigation.PendingNavigation
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.gateway.TestSignInGateway
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestPendingAccountWipe
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.settings.api.navigation.AccountDeletedNavKey
import com.tailormyresume.feature.settings.api.navigation.DeleteAccountNavKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteAccountPendingWipeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = TestSessionRepository().apply { sendAccount(SignInAccount.localAccount) }
    private val marker = TestPendingAccountWipe()
    private var wipeFailures = 0
    private var wipes = 0
    private var signOutFailures = 0

    private val deleter = object : ServerAccountDeleter {
        override val deletesRemoteData = true

        override suspend fun delete() = Result.success(Unit)

        override suspend fun isClosed() = Result.success(true)
    }

    private val finisher = AccountWipeFinisher {
        wipes++
        if (wipeFailures > 0) {
            wipeFailures--
            throw IllegalStateException("wipe failed")
        }
    }

    @Before
    fun clearPendingNavigation() {
        PendingNavigation.consume()
    }

    @After
    fun dropPendingNavigation() {
        PendingNavigation.consume()
    }

    @Test
    fun localWipePendingShowsFinishRetry() = runTest {
        signOutFailures = 1
        wipeFailures = Int.MAX_VALUE
        val viewModel = enteredViewModel()

        viewModel.onDeleteTapped()
        viewModel.onDeleteConfirmed()

        assertThat(viewModel.ready().failure).isEqualTo(DeleteAccountFailure.LOCAL_WIPE_PENDING)
        assertThat(PendingNavigation.consume()).isEmpty()
        assertThat(marker.current).isEqualTo(PendingWipeState.SERVER_CLOSED)
    }

    @Test
    fun retryFinishesWipeAndNavigates() = runTest {
        signOutFailures = 1
        wipeFailures = Int.MAX_VALUE
        val viewModel = enteredViewModel()
        viewModel.onDeleteTapped()
        viewModel.onDeleteConfirmed()
        wipeFailures = 0

        viewModel.onFinishRemovalTapped()

        assertThat(PendingNavigation.consume()).containsExactly(AccountDeletedNavKey)
        assertThat(marker.current).isEqualTo(PendingWipeState.NONE)
    }

    @Test
    fun aFailedRetryStaysOnTheFinishRemovalState() = runTest {
        signOutFailures = 1
        wipeFailures = Int.MAX_VALUE
        val viewModel = enteredViewModel()
        viewModel.onDeleteTapped()
        viewModel.onDeleteConfirmed()

        viewModel.onFinishRemovalTapped()

        assertThat(viewModel.ready().failure).isEqualTo(DeleteAccountFailure.LOCAL_WIPE_PENDING)
        assertThat(PendingNavigation.consume()).isEmpty()
        assertThat(marker.current).isEqualTo(PendingWipeState.SERVER_CLOSED)
    }

    @Test
    fun finishRemovalDoesNothingWhenNothingFailed() = runTest {
        val viewModel = enteredViewModel()

        viewModel.onFinishRemovalTapped()

        assertThat(wipes).isEqualTo(0)
        assertThat(PendingNavigation.consume()).isEmpty()
    }

    @Test
    fun enterWithPendingMarkerResumes() = runTest {
        marker.current = PendingWipeState.SERVER_CLOSED

        enteredViewModel()

        assertThat(wipes).isEqualTo(1)
        assertThat(marker.current).isEqualTo(PendingWipeState.NONE)
        assertThat(PendingNavigation.consume()).containsExactly(AccountDeletedNavKey)
    }

    @Test
    fun enterWithPendingMarkerAndFailingWipeShowsFinishRemoval() = runTest {
        marker.current = PendingWipeState.SERVER_CLOSED
        wipeFailures = Int.MAX_VALUE

        val viewModel = enteredViewModel()

        assertThat(viewModel.ready().failure).isEqualTo(DeleteAccountFailure.LOCAL_WIPE_PENDING)
        assertThat(PendingNavigation.consume()).isEmpty()
    }

    @Test
    fun enterWithoutMarkerDoesNotWipe() = runTest {
        enteredViewModel()

        assertThat(wipes).isEqualTo(0)
    }

    @Test
    fun enterWithRequestedMarkerNeverWipes() = runTest {
        marker.current = PendingWipeState.REQUESTED

        val viewModel = enteredViewModel()

        assertThat(wipes).isEqualTo(0)
        assertThat(viewModel.ready().failure).isNull()
    }

    private fun TestScope.enteredViewModel(): DeleteAccountViewModel {
        val viewModel = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        viewModel.onEnter(DeleteAccountNavKey(scenario = DebugScenario.DEFAULT))
        return viewModel
    }

    private fun DeleteAccountViewModel.ready(): DeleteAccountUiState.Ready =
        uiState.value as DeleteAccountUiState.Ready

    private fun viewModel(): DeleteAccountViewModel {
        val signIn = TestSignInGateway(session)
        val gateway = object : SignInGateway by signIn {
            override suspend fun signOut() {
                if (signOutFailures > 0) {
                    signOutFailures--
                    throw IllegalStateException("sign out failed")
                }
                signIn.signOut()
            }
        }
        return DeleteAccountViewModel(
            connectivityMonitor = TestConnectivityMonitor(),
            sessionRepository = session,
            deleteAccount = DeleteAccountUseCase(
                applicationRepository = TestApplicationRepository(),
                profileRepository = TestProfileRepository(),
                exportHistoryRepository = TestExportHistoryRepository(),
                sessionRepository = session,
                signInGateway = gateway,
                serverAccountDeleter = deleter,
                creditBalance = AccountCreditBalance(TestPaymentGateway().withFreeCredits(2)),
                latency = NoMockLatency,
                pendingWipe = marker,
                finishPendingWipe = FinishPendingAccountWipeUseCase(marker, finisher, deleter),
            ),
        )
    }
}
