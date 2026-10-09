package com.tailormyresume.feature.onboarding.impl.welcome

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.onboarding.api.navigation.WelcomeNavKey
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class WelcomeViewModelConsentForwardTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = TestSessionRepository()
    private val viewModel = WelcomeViewModel(NextOnboardingStepUseCase(session, TestProfileRepository()), TestConnectivityMonitor(), session)

    private fun consentUnder(version: String) =
        ConsentRecord(setOf(ConsentPurpose.AI_PROCESSING), Instant.fromEpochMilliseconds(1), version)

    @Test
    fun aSignedInUserWithAnOlderNoticeIsSentToConsent() = runTest {
        session.saveAccount(SignInAccount.localAccount)
        session.markOnboardingComplete()
        session.recordConsent(consentUnder("2026-09-a"))

        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.destination).isEqualTo(WelcomeDestination.CONSENT)
    }

    @Test
    fun aSignedInUserWithTheCurrentNoticeStaysOnWelcome() = runTest {
        session.saveAccount(SignInAccount.localAccount)
        session.recordConsent(consentUnder(ConsentRecord.CURRENT_NOTICE_VERSION))

        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.destination).isNull()
    }

    @Test
    fun aSignedOutUserStaysOnWelcome() = runTest {
        viewModel.onEnter(WelcomeNavKey(scenario = DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.destination).isNull()
    }
}
