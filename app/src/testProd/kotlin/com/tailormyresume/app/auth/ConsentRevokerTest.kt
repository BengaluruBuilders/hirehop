package com.tailormyresume.app.auth

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class ConsentRevokerTest {
    @Test
    fun aConsentRequiredAnswerSendsTheUserBackToTheConsentScreen() = runTest(UnconfinedTestDispatcher()) {
        val session = TestSessionRepository()
        session.saveAccount(SignInAccount("uid-1", "Priya", "p@example.com"))
        session.recordConsent(ConsentRecord(setOf(ConsentPurpose.AI_PROCESSING), Instant.fromEpochMilliseconds(1), "2026-10-b"))
        val nextStep = NextOnboardingStepUseCase(session, TestProfileRepository())

        ConsentRevoker(session, TestScope(testScheduler)).onConsentRequired()
        testScheduler.advanceUntilIdle()

        assertThat(nextStep()).isEqualTo(OnboardingStep.Consent)
    }
}
