package com.hirehop.feature.settings.impl.settings

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.SignInAccount
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.gateway.TestPaymentGateway
import com.hirehop.core.testing.gateway.TestSignInGateway
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.settings.api.navigation.SettingsNavKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository = TestSessionRepository().apply { sendAccount(SignInAccount.localAccount) }
    private val connectivity = TestConnectivityMonitor()
    private val paymentGateway = TestPaymentGateway().withFreeCredits(CREDITS)
    private val signInGateway = TestSignInGateway(sessionRepository)
    private val viewModel by lazy {
        SettingsViewModel(
            sessionRepository = sessionRepository,
            paymentGateway = paymentGateway,
            connectivityMonitor = connectivity,
            signInGateway = signInGateway,
        )
    }

    @Test
    fun content_readsTheAccountAndTheRealCreditTotal() = runTest {
        collectState()

        val content = viewModel.content()
        assertThat(content.account).isEqualTo(SignInAccount.localAccount)
        assertThat(content.creditsLeft).isEqualTo(CREDITS)
        assertThat(content.isOffline).isFalse()
        assertThat(content.isSignOutConfirmVisible).isFalse()
    }

    @Test
    fun content_withoutAnAccount_hasNoAccount() = runTest {
        sessionRepository.sendAccount(null)
        collectState()

        assertThat(viewModel.content().account).isNull()
    }

    @Test
    fun content_carriesTheDateTheConsentWasGiven() = runTest {
        sessionRepository.sendConsent(
            ConsentRecord(
                purposes = setOf(ConsentPurpose.READ_AND_BUILD),
                acceptedAt = CONSENT_TIME,
                noticeVersion = ConsentRecord.CURRENT_NOTICE_VERSION,
            ),
        )
        collectState()

        assertThat(viewModel.content().consentAcceptedAt).isEqualTo(CONSENT_TIME)
    }

    @Test
    fun content_withoutConsent_hasNoConsentDate() = runTest {
        collectState()

        assertThat(viewModel.content().consentAcceptedAt).isNull()
    }

    @Test
    fun content_whenTheDeviceGoesOffline_isOffline() = runTest {
        collectState()

        connectivity.setOnline(false)

        assertThat(viewModel.content().isOffline).isTrue()
    }

    @Test
    fun onEnter_withTheOfflineScenario_forcesOffline() = runTest {
        collectState()

        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.OFFLINE))

        assertThat(viewModel.content().isOffline).isTrue()
    }

    @Test
    fun signOutRequested_showsTheConfirmDialog() = runTest {
        collectState()

        viewModel.onSignOutRequested()

        assertThat(viewModel.content().isSignOutConfirmVisible).isTrue()
    }

    @Test
    fun signOutDismissed_keepsTheAccount() = runTest {
        collectState()
        viewModel.onSignOutRequested()

        viewModel.onSignOutDismissed()

        assertThat(viewModel.content().isSignOutConfirmVisible).isFalse()
        assertThat(viewModel.content().account).isEqualTo(SignInAccount.localAccount)
    }

    @Test
    fun signOutConfirmed_signsOutAndClosesTheDialog() = runTest {
        collectState()
        viewModel.onSignOutRequested()

        viewModel.onSignOutConfirmed()

        assertThat(viewModel.content().isSignOutConfirmVisible).isFalse()
        assertThat(viewModel.content().account).isNull()
    }

    @Test
    fun signOutRequested_whenOffline_isIgnored() = runTest {
        collectState()
        connectivity.setOnline(false)

        viewModel.onSignOutRequested()

        assertThat(viewModel.content().isSignOutConfirmVisible).isFalse()
    }

    private fun TestScope.collectState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private fun SettingsViewModel.content(): SettingsUiState.Content =
        uiState.value as SettingsUiState.Content

    private companion object {
        const val CREDITS = 4
        val CONSENT_TIME = Instant.fromEpochSeconds(1_802_606_400)
    }
}
