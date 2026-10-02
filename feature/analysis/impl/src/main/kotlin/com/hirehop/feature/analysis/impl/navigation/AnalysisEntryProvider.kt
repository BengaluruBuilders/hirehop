package com.hirehop.feature.analysis.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.analysis.api.navigation.AnalysisNavKey
import com.hirehop.feature.analysis.impl.AnalysisRoute
import com.hirehop.feature.onboarding.api.navigation.ConfirmFactsNavKey
import com.hirehop.feature.onboarding.api.navigation.ConsentNavKey
import com.hirehop.feature.onboarding.api.navigation.ImportResumeNavKey
import com.hirehop.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import com.hirehop.feature.onboarding.api.navigation.SignInNavKey
import com.hirehop.feature.profile.api.navigation.navigateToFactEditor
import com.hirehop.feature.tailor.api.navigation.navigateToTailor

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
