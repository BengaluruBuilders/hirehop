package com.tailormyresume.app.ui

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.onboarding.ObserveStartDestinationUseCase
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class AppViewModelConsentTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val sessionRepository = TestSessionRepository()

    private fun viewModel() = AppViewModel(ObserveStartDestinationUseCase(sessionRepository))

    @Test
    fun rootState_afterSignOutAndSignInWithoutConsent_staysFirstRunUntilConsentIsRecorded() = runTest {
        val consent = ConsentRecord(setOf(ConsentPurpose.AI_PROCESSING), Instant.fromEpochMilliseconds(1), "2026-10-b")
        sessionRepository.saveAccount(SignInAccount.localAccount)
        sessionRepository.markOnboardingComplete()
        sessionRepository.recordConsent(consent)

        viewModel().rootState.test {
            assertThat(awaitItem()).isEqualTo(AppRootState.Loading)
            assertThat(awaitItem()).isEqualTo(AppRootState.Main)

            sessionRepository.signOut()
            sessionRepository.clearConsent()
            assertThat(awaitItem()).isEqualTo(AppRootState.FirstRun)

            sessionRepository.saveAccount(SignInAccount.localAccount)
            advanceUntilIdle()
            expectNoEvents()

            sessionRepository.recordConsent(consent)
            assertThat(awaitItem()).isEqualTo(AppRootState.Main)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
