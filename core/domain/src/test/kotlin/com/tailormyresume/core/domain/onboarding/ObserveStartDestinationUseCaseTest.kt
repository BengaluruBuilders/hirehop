package com.tailormyresume.core.domain.onboarding

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class ObserveStartDestinationUseCaseTest {

    private val session = TestSessionRepository()
    private val useCase = ObserveStartDestinationUseCase(session)
    private val consent = ConsentRecord(setOf(ConsentPurpose.AI_PROCESSING), Instant.fromEpochMilliseconds(1), "2026-10-b")

    @Test
    fun withOnboardingIncompleteTheStartIsWelcome() = runTest {
        useCase().test {
            assertThat(awaitItem()).isEqualTo(StartDestination.Welcome)
        }
    }

    @Test
    fun withOnboardingCompleteTheStartIsApplications() = runTest {
        session.saveAccount(SignInAccount.localAccount)
        session.markOnboardingComplete()
        session.recordConsent(consent)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(StartDestination.Applications)
        }
    }

    @Test
    fun signingOutReturnsTheStartToWelcome() = runTest {
        session.saveAccount(SignInAccount.localAccount)
        session.markOnboardingComplete()
        session.recordConsent(consent)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(StartDestination.Applications)
            session.signOut()
            assertThat(awaitItem()).isEqualTo(StartDestination.Welcome)
        }
    }

    @Test
    fun signingInAgainAfterSignOutReturnsTheStartToApplications() = runTest {
        session.saveAccount(SignInAccount.localAccount)
        session.markOnboardingComplete()
        session.recordConsent(consent)
        session.signOut()

        useCase().test {
            assertThat(awaitItem()).isEqualTo(StartDestination.Welcome)
            session.saveAccount(SignInAccount.localAccount)
            assertThat(awaitItem()).isEqualTo(StartDestination.Applications)
        }
    }

    @Test
    fun withOnboardingCompleteAndNoAccountTheStartIsWelcome() = runTest {
        session.markOnboardingComplete()

        useCase().test {
            assertThat(awaitItem()).isEqualTo(StartDestination.Welcome)
        }
    }

    @Test
    fun clearingTheSessionReturnsTheStartToWelcome() = runTest {
        session.saveAccount(SignInAccount.localAccount)
        session.markOnboardingComplete()
        session.recordConsent(consent)

        useCase().test {
            assertThat(awaitItem()).isEqualTo(StartDestination.Applications)
            session.clear()
            assertThat(awaitItem()).isEqualTo(StartDestination.Welcome)
        }
    }
}
