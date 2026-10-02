package com.hirehop.feature.profile.impl

import androidx.navigation3.runtime.NavKey
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.feature.analysis.api.navigation.DefaultAnalysisNavKey
import com.hirehop.feature.applications.api.navigation.DefaultApplicationsNavKey
import com.hirehop.feature.onboarding.api.navigation.ConfirmFactsNavKey
import com.hirehop.feature.onboarding.api.navigation.ConsentNavKey
import com.hirehop.feature.onboarding.api.navigation.ImportResumeNavKey
import com.hirehop.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import com.hirehop.feature.onboarding.api.navigation.SignInNavKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed interface ProfileExit {
    data object Profile : ProfileExit

    data class Step(val key: NavKey) : ProfileExit
}

class ProfileExitResolver @Inject constructor(
    private val nextOnboardingStep: NextOnboardingStepUseCase,
    private val sessionRepository: SessionRepository,
) {
    suspend fun resolve(): ProfileExit {
        if (sessionRepository.observeOnboardingComplete().first()) return ProfileExit.Profile
        return ProfileExit.Step(nextOnboardingStep().toNavKey())
    }
}

internal fun OnboardingStep.toNavKey(): NavKey = when (this) {
    OnboardingStep.SignIn -> SignInNavKey()
    OnboardingStep.Consent -> ConsentNavKey()
    OnboardingStep.ImportResume -> ImportResumeNavKey()
    OnboardingStep.ConfirmFacts -> ConfirmFactsNavKey()
    is OnboardingStep.GapAnalysis -> DefaultAnalysisNavKey
    OnboardingStep.PasteJobDescription -> PasteJobDescriptionNavKey()
    OnboardingStep.Applications -> DefaultApplicationsNavKey
}
