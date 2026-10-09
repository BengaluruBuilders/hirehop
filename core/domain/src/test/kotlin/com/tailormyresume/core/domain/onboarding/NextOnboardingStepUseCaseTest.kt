package com.tailormyresume.core.domain.onboarding

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.FakeProfileRepository
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.KeptJobDescription
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.data.canonicalProfileWithoutEntries
import com.tailormyresume.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class NextOnboardingStepUseCaseTest {

    private val session = TestSessionRepository()
    private val profile = FakeProfileRepository()
    private val useCase = NextOnboardingStepUseCase(session, profile)
    private val job = KeptJobDescription(text = "Analyst\nSQL", company = "Northwind GCC", role = "Analyst")

    private suspend fun signedInWithConsent() {
        session.saveAccount(SignInAccount.localAccount)
        session.recordConsent(ConsentRecord(setOf(ConsentPurpose.READ_AND_BUILD), Instant.fromEpochSeconds(1), ConsentRecord.CURRENT_NOTICE_VERSION))
    }

    private fun unconfirmed(): CandidateProfile = canonicalCandidateProfile.copy(
        entries = canonicalCandidateProfile.entries.map { it.copy(isConfirmed = false) },
    )

    @Test
    fun aSignedOutCandidateGoesToSignIn() = runTest {
        assertThat(useCase()).isEqualTo(OnboardingStep.SignIn)
    }

    @Test
    fun aSignedInCandidateWithNoConsentGoesToConsent() = runTest {
        session.saveAccount(SignInAccount.localAccount)

        assertThat(useCase()).isEqualTo(OnboardingStep.Consent)
    }

    @Test
    fun aCandidateWithNoProfileGoesToImportResume() = runTest {
        signedInWithConsent()

        assertThat(useCase()).isEqualTo(OnboardingStep.ImportResume)
    }

    @Test
    fun aProfileWithNoEntriesGoesToImportResume() = runTest {
        signedInWithConsent()
        profile.saveProfile(canonicalProfileWithoutEntries)

        assertThat(useCase()).isEqualTo(OnboardingStep.ImportResume)
    }

    @Test
    fun aProfileWithNoConfirmedFactGoesToConfirmFacts() = runTest {
        signedInWithConsent()
        profile.saveProfile(unconfirmed())

        assertThat(useCase()).isEqualTo(OnboardingStep.ConfirmFacts)
    }

    @Test
    fun aKeptJobDescriptionGoesToGapAnalysisWithThatJob() = runTest {
        signedInWithConsent()
        profile.saveProfile(canonicalCandidateProfile)
        session.keepJobDescription(job)

        assertThat(useCase()).isEqualTo(OnboardingStep.GapAnalysis(job))
    }

    @Test
    fun withNoKeptJobDescriptionTheFirstRunGoesToPasteJobDescription() = runTest {
        signedInWithConsent()
        profile.saveProfile(canonicalCandidateProfile)

        assertThat(useCase()).isEqualTo(OnboardingStep.PasteJobDescription)
    }

    @Test
    fun withOnboardingCompleteAndNoKeptJobDescriptionTheNextStepIsApplications() = runTest {
        signedInWithConsent()
        profile.saveProfile(canonicalCandidateProfile)
        session.markOnboardingComplete()

        assertThat(useCase()).isEqualTo(OnboardingStep.Applications)
    }

    @Test
    fun signInComesBeforeEveryOtherStep() = runTest {
        profile.saveProfile(canonicalCandidateProfile)
        session.keepJobDescription(job)
        session.markOnboardingComplete()

        assertThat(useCase()).isEqualTo(OnboardingStep.SignIn)
    }

    @Test
    fun theObservedStepFollowsTheSession() = runTest {
        profile.saveProfile(canonicalCandidateProfile)

        useCase.observe().test {
            assertThat(awaitItem()).isEqualTo(OnboardingStep.SignIn)
            session.saveAccount(SignInAccount.localAccount)
            assertThat(awaitItem()).isEqualTo(OnboardingStep.Consent)
            session.recordConsent(ConsentRecord(setOf(ConsentPurpose.READ_AND_BUILD), Instant.fromEpochSeconds(1), ConsentRecord.CURRENT_NOTICE_VERSION))
            assertThat(awaitItem()).isEqualTo(OnboardingStep.PasteJobDescription)
        }
    }
}
