package com.tailormyresume.core.domain.onboarding

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.FakeProfileRepository
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class NextOnboardingStepOlderNoticeTest {

    private val session = TestSessionRepository()
    private val profile = FakeProfileRepository()
    private val useCase = NextOnboardingStepUseCase(session, profile)

    private fun consentUnder(version: String) =
        ConsentRecord(setOf(ConsentPurpose.AI_PROCESSING), Instant.fromEpochMilliseconds(1), version)

    private suspend fun signedInWithProfile() {
        session.saveAccount(SignInAccount.localAccount)
        profile.saveProfile(canonicalCandidateProfile)
    }

    @Test
    fun anOlderNoticeVersionGoesToConsent() = runTest {
        signedInWithProfile()
        session.recordConsent(consentUnder("2026-09-a"))

        assertThat(useCase()).isEqualTo(OnboardingStep.Consent)
    }

    @Test
    fun agreeingAgainUnderTheCurrentNoticeMovesOn() = runTest {
        signedInWithProfile()
        session.recordConsent(consentUnder("2026-09-a"))
        session.recordConsent(consentUnder(ConsentRecord.CURRENT_NOTICE_VERSION))

        assertThat(useCase()).isEqualTo(OnboardingStep.PasteJobDescription)
    }

    @Test
    fun aSignedOutCandidateStillGoesToSignInWhateverTheNoticeVersion() = runTest {
        session.recordConsent(consentUnder("2026-09-a"))

        assertThat(useCase()).isEqualTo(OnboardingStep.SignIn)
    }
}
