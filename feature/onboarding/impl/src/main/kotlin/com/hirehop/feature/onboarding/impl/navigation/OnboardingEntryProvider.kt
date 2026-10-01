package com.hirehop.feature.onboarding.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.analysis.api.navigation.DefaultAnalysisNavKey
import com.hirehop.feature.onboarding.api.navigation.ConfirmFactsNavKey
import com.hirehop.feature.onboarding.api.navigation.ConsentNavKey
import com.hirehop.feature.onboarding.api.navigation.ImportResumeNavKey
import com.hirehop.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import com.hirehop.feature.onboarding.api.navigation.SignInNavKey
import com.hirehop.feature.onboarding.api.navigation.WelcomeNavKey
import com.hirehop.feature.onboarding.api.navigation.navigateToConfirmFacts
import com.hirehop.feature.onboarding.api.navigation.navigateToConsent
import com.hirehop.feature.onboarding.api.navigation.navigateToImportResume
import com.hirehop.feature.onboarding.api.navigation.navigateToPasteJobDescription
import com.hirehop.feature.onboarding.api.navigation.navigateToSignIn
import com.hirehop.feature.onboarding.impl.confirmfacts.ConfirmFactsRoute
import com.hirehop.feature.onboarding.impl.consent.ConsentRoute
import com.hirehop.feature.onboarding.impl.importresume.ImportResumeRoute
import com.hirehop.feature.onboarding.impl.pastejd.PasteJobDescriptionRoute
import com.hirehop.feature.onboarding.impl.signin.SignInRoute
import com.hirehop.feature.onboarding.impl.welcome.WelcomeRoute
import com.hirehop.feature.profile.api.navigation.navigateToGuidedProfileForm

fun EntryProviderScope<NavKey>.onboardingEntry(navigator: Navigator) {
    entry<WelcomeNavKey> { key ->
        WelcomeRoute(
            key = key,
            onNavigateToPasteJobDescription = { navigator.navigateToPasteJobDescription() },
            onNavigateToImportResume = { navigator.navigateToImportResume() },
            onNavigateToBuildProfileStepByStep = { navigator.navigateToGuidedProfileForm() },
        )
    }
    entry<PasteJobDescriptionNavKey> { key ->
        PasteJobDescriptionRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
            onAnalyseRequested = { navigator.navigateToSignIn() },
        )
    }
    entry<SignInNavKey> { key ->
        SignInRoute(
            key = key,
            onSkipToJobDescription = { navigator.navigateToConsent() },
        )
    }
    entry<ConsentNavKey> { key ->
        ConsentRoute(
            key = key,
            onSkipToJobDescription = { navigator.navigateToImportResume() },
        )
    }
    entry<ImportResumeNavKey> { key ->
        ImportResumeRoute(
            key = key,
            onBack = { navigator.goBack() },
            onGoToGuidedForm = { navigator.navigateToGuidedProfileForm() },
            onReviewFacts = { navigator.navigateToConfirmFacts() },
        )
    }
    entry<ConfirmFactsNavKey> { key ->
        ConfirmFactsRoute(
            key = key,
            onBack = { navigator.goBack() },
            onContinue = { navigator.navigate(DefaultAnalysisNavKey) },
            onImportResume = { navigator.navigateToImportResume() },
            onEditFact = { factId, category ->
                navigator.navigate(
                    com.hirehop.feature.profile.api.navigation.FactEditorNavKey(
                        entryId = factId,
                        entryType = category,
                    ),
                )
            },
        )
    }
}
