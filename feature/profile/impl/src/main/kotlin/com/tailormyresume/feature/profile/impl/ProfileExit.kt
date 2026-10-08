package com.tailormyresume.feature.profile.impl

import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.feature.analysis.api.navigation.DefaultAnalysisNavKey
import com.tailormyresume.feature.applications.api.navigation.DefaultApplicationsNavKey
import com.tailormyresume.feature.onboarding.api.navigation.ConfirmFactsNavKey
import com.tailormyresume.feature.onboarding.api.navigation.ConsentNavKey
import com.tailormyresume.feature.onboarding.api.navigation.ImportResumeNavKey
import com.tailormyresume.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import com.tailormyresume.feature.onboarding.api.navigation.SignInNavKey
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
