package com.tailormyresume.core.domain.onboarding

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveStartDestinationUseCaseTest {

    private val session = TestSessionRepository()
    private val useCase = ObserveStartDestinationUseCase(session)

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

        useCase().test {
            assertThat(awaitItem()).isEqualTo(StartDestination.Applications)
        }
    }

    @Test
    fun signingOutReturnsTheStartToWelcome() = runTest {
        session.saveAccount(SignInAccount.localAccount)
        session.markOnboardingComplete()

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

        useCase().test {
            assertThat(awaitItem()).isEqualTo(StartDestination.Applications)
            session.clear()
            assertThat(awaitItem()).isEqualTo(StartDestination.Welcome)
        }
    }
}
