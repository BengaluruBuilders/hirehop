package com.tailormyresume.feature.analysis.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.core.navigation.Navigator
import com.tailormyresume.feature.analysis.api.navigation.AnalysisNavKey
import com.tailormyresume.feature.analysis.impl.AnalysisRoute
import com.tailormyresume.feature.onboarding.api.navigation.ConfirmFactsNavKey
import com.tailormyresume.feature.onboarding.api.navigation.ConsentNavKey
import com.tailormyresume.feature.onboarding.api.navigation.ImportResumeNavKey
import com.tailormyresume.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import com.tailormyresume.feature.onboarding.api.navigation.SignInNavKey
import com.tailormyresume.feature.profile.api.navigation.navigateToFactEditor
import com.tailormyresume.feature.tailor.api.navigation.navigateToTailor

fun EntryProviderScope<NavKey>.analysisEntry(navigator: Navigator) {
    entry<AnalysisNavKey> { key ->
        AnalysisRoute(
            scenario = key.scenario,
            onBackClick = navigator::goBack,
            onLeave = { step -> navigator.leaveTo(step.toNavKey()) },
            onOpenTailor = navigator::openTailorFromAnalysis,
            onEditFact = navigator::navigateToFactEditor,
        )
    }
}

internal fun Navigator.openTailorFromAnalysis(applicationId: String) {
    navigate(state.currentTopLevelKey)
    navigateToTailor(applicationId)
}

private fun Navigator.leaveTo(key: NavKey?) {
    if (key == null) goBack() else replace(key)
}

private fun OnboardingStep.toNavKey(): NavKey? = when (this) {
    OnboardingStep.SignIn -> SignInNavKey()
    OnboardingStep.Consent -> ConsentNavKey()
    OnboardingStep.ImportResume -> ImportResumeNavKey()
    OnboardingStep.ConfirmFacts -> ConfirmFactsNavKey()
    OnboardingStep.PasteJobDescription -> PasteJobDescriptionNavKey()
    is OnboardingStep.GapAnalysis, OnboardingStep.Applications -> null
}
