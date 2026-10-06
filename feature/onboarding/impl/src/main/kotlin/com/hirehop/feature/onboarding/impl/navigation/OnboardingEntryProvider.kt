package com.hirehop.feature.onboarding.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.analysis.api.navigation.DefaultAnalysisNavKey
import com.hirehop.feature.applications.api.navigation.DefaultApplicationsNavKey
import com.hirehop.feature.onboarding.api.navigation.ConfirmFactsNavKey
import com.hirehop.feature.onboarding.api.navigation.ConsentNavKey
import com.hirehop.feature.onboarding.api.navigation.ImportResumeNavKey
import com.hirehop.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import com.hirehop.feature.onboarding.api.navigation.SignInNavKey
import com.hirehop.feature.onboarding.api.navigation.WelcomeNavKey
import com.hirehop.feature.onboarding.api.navigation.navigateToConfirmFacts
import com.hirehop.feature.onboarding.api.navigation.navigateToConsent
import com.hirehop.feature.onboarding.api.navigation.navigateToPasteJobDescription
import com.hirehop.feature.onboarding.api.navigation.navigateToSignIn
import com.hirehop.feature.onboarding.api.navigation.navigateToWelcome
import com.hirehop.feature.onboarding.impl.confirmfacts.ConfirmFactsRoute
import com.hirehop.feature.onboarding.impl.consent.ConsentRoute
import com.hirehop.feature.onboarding.impl.importresume.ImportResumeRoute
import com.hirehop.feature.onboarding.impl.pastejd.PasteJobDescriptionRoute
import com.hirehop.feature.onboarding.impl.signin.SignInRoute
import com.hirehop.feature.onboarding.impl.welcome.WelcomeRoute
import com.hirehop.feature.profile.api.navigation.FactEditorNavKey
import com.hirehop.feature.profile.api.navigation.navigateToGuidedProfileForm

fun EntryProviderScope<NavKey>.onboardingEntry(navigator: Navigator) {
    entry<WelcomeNavKey> { key ->
        WelcomeRoute(
            key = key,
            onNavigateToPasteJobDescription = { navigator.navigateToPasteJobDescription() },
            onNavigateToSignIn = { navigator.navigateToSignIn() },
            onNavigateToConsent = { navigator.navigateToConsent() },
        )
    }
    entry<PasteJobDescriptionNavKey> { key ->
        PasteJobDescriptionRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
            onNavigateToStep = { step -> navigator.navigateToStep(step) },
        )
    }
    entry<SignInNavKey> { key ->
        SignInRoute(
            key = key,
            onBack = { navigator.goBack() },
            onBackToStart = { navigator.navigateToWelcome() },
            onNavigateToStep = { step -> navigator.replaceWithStep(step) },
        )
    }
    entry<ConsentNavKey> { key ->
        ConsentRoute(
            key = key,
            onBack = { navigator.goBack() },
            onNavigateToStep = { step -> navigator.replaceWithStep(step) },
        )
    }
    entry<ImportResumeNavKey> { key ->
        ImportResumeRoute(
            key = key,
            onBack = { navigator.goBack() },
            onGoToGuidedForm = { resumedFromScan -> navigator.navigateToGuidedProfileForm(resumedFromScan) },
            onReviewFacts = { navigator.navigateToConfirmFacts() },
        )
    }
    entry<ConfirmFactsNavKey> { key ->
        ConfirmFactsRoute(
            key = key,
            onBack = { navigator.goBack() },
            onNavigateToStep = { step -> navigator.navigateToStep(step) },
            onImportResume = { navigator.replace(ImportResumeNavKey()) },
            onEditFact = { factId, category ->
                navigator.navigate(FactEditorNavKey(entryId = factId, entryType = category))
            },
        )
    }
}

private fun Navigator.navigateToStep(step: OnboardingStep) {
    navigate(step.toNavKey())
}

private fun Navigator.replaceWithStep(step: OnboardingStep) {
    replace(step.toNavKey())
}

private fun OnboardingStep.toNavKey(): NavKey = when (this) {
    OnboardingStep.SignIn -> SignInNavKey()
    OnboardingStep.Consent -> ConsentNavKey()
    OnboardingStep.ImportResume -> ImportResumeNavKey()
    OnboardingStep.ConfirmFacts -> ConfirmFactsNavKey()
    is OnboardingStep.GapAnalysis -> DefaultAnalysisNavKey
    OnboardingStep.PasteJobDescription -> PasteJobDescriptionNavKey()
    OnboardingStep.Applications -> DefaultApplicationsNavKey
}
