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

class ObserveStartDestinationOlderNoticeTest {

    private val session = TestSessionRepository()
    private val useCase = ObserveStartDestinationUseCase(session)

    private fun consentUnder(version: String) =
        ConsentRecord(setOf(ConsentPurpose.AI_PROCESSING), Instant.fromEpochMilliseconds(1), version)

    private suspend fun signedInAndOnboarded() {
        session.saveAccount(SignInAccount.localAccount)
        session.markOnboardingComplete()
    }

    @Test
    fun anOlderNoticeVersionSendsTheStartBackToWelcome() = runTest {
        signedInAndOnboarded()
        session.recordConsent(consentUnder("2026-09-a"))

        useCase().test {
            assertThat(awaitItem()).isEqualTo(StartDestination.Welcome)
        }
    }

    @Test
    fun agreeingAgainUnderTheCurrentNoticeReturnsToApplications() = runTest {
        signedInAndOnboarded()
        session.recordConsent(consentUnder("2026-09-a"))

        useCase().test {
            assertThat(awaitItem()).isEqualTo(StartDestination.Welcome)
            session.recordConsent(consentUnder(ConsentRecord.CURRENT_NOTICE_VERSION))
            assertThat(awaitItem()).isEqualTo(StartDestination.Applications)
        }
    }
}
