package com.tailormyresume.feature.settings.impl.deleteaccount

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.mock.NoMockLatency
import com.tailormyresume.core.data.repository.PendingWipeState
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
class DeleteAccountUnconfirmedCloseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = TestSessionRepository().apply { sendAccount(SignInAccount.localAccount) }
    private val marker = TestPendingAccountWipe(PendingWipeState.REQUESTED)
    private var probe: Result<Boolean> = Result.failure(IllegalStateException("401"))
    private var wipes = 0

    private val deleter = object : ServerAccountDeleter {
        override val deletesRemoteData = true

        override suspend fun delete(): Result<Unit> = Result.failure(IllegalStateException("401"))

        override suspend fun isClosed(): Result<Boolean> = probe
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
    fun anUndecidedRequestedMarkerShowsTheUnconfirmedCloseState() = runTest {
        val viewModel = enteredViewModel()

        viewModel.onDeleteTapped()
        viewModel.onDeleteConfirmed()

        assertThat(viewModel.ready().failure).isEqualTo(DeleteAccountFailure.CLOSE_UNCONFIRMED)
        assertThat(marker.current).isEqualTo(PendingWipeState.REQUESTED)
        assertThat(wipes).isEqualTo(0)
        assertThat(PendingNavigation.consume()).isEmpty()
    }

    @Test
    fun tryAgainReprobesAndFinishesWhenTheAccountIsClosed() = runTest {
        val viewModel = enteredViewModel()
        viewModel.onDeleteTapped()
        viewModel.onDeleteConfirmed()
        probe = Result.success(true)

        viewModel.onFinishRemovalTapped()

        assertThat(wipes).isEqualTo(1)
        assertThat(marker.current).isEqualTo(PendingWipeState.NONE)
        assertThat(PendingNavigation.consume()).containsExactly(AccountDeletedNavKey)
    }

    @Test
    fun tryAgainStaysUnconfirmedWhileTheProbeStillFails() = runTest {
        val viewModel = enteredViewModel()
        viewModel.onDeleteTapped()
        viewModel.onDeleteConfirmed()

        viewModel.onFinishRemovalTapped()

        assertThat(viewModel.ready().failure).isEqualTo(DeleteAccountFailure.CLOSE_UNCONFIRMED)
        assertThat(wipes).isEqualTo(0)
    }

    @Test
    fun tryAgainReturnsToTheNormalScreenWhenTheAccountIsOpen() = runTest {
        val viewModel = enteredViewModel()
        viewModel.onDeleteTapped()
        viewModel.onDeleteConfirmed()
        probe = Result.success(false)

        viewModel.onFinishRemovalTapped()

        assertThat(viewModel.ready().failure).isNull()
        assertThat(marker.current).isEqualTo(PendingWipeState.NONE)
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
        val finisher = AccountWipeFinisher { wipes++ }
        return DeleteAccountViewModel(
            connectivityMonitor = TestConnectivityMonitor(),
            sessionRepository = session,
            deleteAccount = DeleteAccountUseCase(
                applicationRepository = TestApplicationRepository(),
                profileRepository = TestProfileRepository(),
                exportHistoryRepository = TestExportHistoryRepository(),
                sessionRepository = session,
                signInGateway = TestSignInGateway(session),
                serverAccountDeleter = deleter,
                creditBalance = AccountCreditBalance(TestPaymentGateway().withFreeCredits(2)),
                latency = NoMockLatency,
                pendingWipe = marker,
                finishPendingWipe = FinishPendingAccountWipeUseCase(marker, finisher, deleter),
            ),
        )
    }
}
