package com.hirehop.feature.profile.impl

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.KeptJobDescription
import com.hirehop.core.model.SignInAccount
import com.hirehop.core.testing.data.sampleProfile
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.feature.analysis.api.navigation.DefaultAnalysisNavKey
import com.hirehop.feature.onboarding.api.navigation.ConsentNavKey
import com.hirehop.feature.onboarding.api.navigation.ImportResumeNavKey
import com.hirehop.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class ProfileExitResolverTest {

    private val session = TestSessionRepository()
    private val profile = TestProfileRepository()
    private val resolver = ProfileExitResolver(NextOnboardingStepUseCase(session, profile), session)

    private val account = SignInAccount(id = "a", displayName = "Priya", email = "p@example.com")
    private val consent = ConsentRecord(
        purposes = ConsentPurpose.entries.toSet(),
        acceptedAt = Instant.fromEpochSeconds(0),
        noticeVersion = ConsentRecord.CURRENT_NOTICE_VERSION,
    )

    @Test
    fun resolve_whenOnboardingIsComplete_goesBackToTheProfile() = runTest {
        session.sendOnboardingComplete(true)

        assertThat(resolver.resolve()).isEqualTo(ProfileExit.Profile)
    }

    @Test
    fun resolve_whenOnboardingIsNotComplete_followsTheNextOnboardingStep() = runTest {
        session.sendAccount(account)
        assertThat(resolver.resolve()).isEqualTo(ProfileExit.Step(ConsentNavKey()))

        session.sendConsent(consent)
        assertThat(resolver.resolve()).isEqualTo(ProfileExit.Step(ImportResumeNavKey()))

        profile.sendProfile(sampleProfile.copy(entries = listOf(sampleProfile.entries.first().copy(isConfirmed = true))))
        assertThat(resolver.resolve()).isEqualTo(ProfileExit.Step(PasteJobDescriptionNavKey()))

        session.keepJobDescription(KeptJobDescription(text = "Analyst", company = "Acme", role = "Analyst"))
        assertThat(resolver.resolve()).isEqualTo(ProfileExit.Step(DefaultAnalysisNavKey))
    }
}
