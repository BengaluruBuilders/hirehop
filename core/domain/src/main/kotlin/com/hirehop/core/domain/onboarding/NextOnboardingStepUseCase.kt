package com.hirehop.core.domain.onboarding

import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.KeptJobDescription
import com.hirehop.core.model.SignInAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class NextOnboardingStepUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(): OnboardingStep = observe().first()

    fun observe(): Flow<OnboardingStep> = combine(
        sessionRepository.observeAccount(),
        sessionRepository.observeConsent(),
        sessionRepository.observeOnboardingComplete(),
        sessionRepository.observeKeptJobDescription(),
        profileRepository.observeProfile(),
        ::decide,
    ).distinctUntilChanged()

    private fun decide(
        account: SignInAccount?,
        consent: ConsentRecord?,
        onboardingComplete: Boolean,
        keptJob: KeptJobDescription?,
        profile: CandidateProfile?,
    ): OnboardingStep {
        val entries = profile?.entries.orEmpty()
        return when {
            account == null -> OnboardingStep.SignIn
            consent == null -> OnboardingStep.Consent
            entries.isEmpty() -> OnboardingStep.ImportResume
            entries.none { it.isConfirmed } -> OnboardingStep.ConfirmFacts
            keptJob != null -> OnboardingStep.GapAnalysis(keptJob)
            onboardingComplete -> OnboardingStep.Applications
            else -> OnboardingStep.PasteJobDescription
        }
    }
}
