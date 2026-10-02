package com.hirehop.feature.onboarding.api.navigation

import androidx.navigation3.runtime.NavKey
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class WelcomeNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class PasteJobDescriptionNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class SignInNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class ConsentNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
    val readOnly: Boolean = false,
) : NavKey

@Serializable
data class ImportResumeNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class ConfirmFactsNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

val DefaultWelcomeNavKey = WelcomeNavKey()

fun Navigator.navigateToWelcome() {
    navigate(DefaultWelcomeNavKey)
}

fun Navigator.navigateToPasteJobDescription() {
    navigate(PasteJobDescriptionNavKey())
}

fun Navigator.navigateToSignIn() {
    navigate(SignInNavKey())
}

fun Navigator.navigateToConsent(readOnly: Boolean = false) {
    navigate(ConsentNavKey(readOnly = readOnly))
}

fun Navigator.navigateToImportResume() {
    navigate(ImportResumeNavKey())
}

fun Navigator.navigateToConfirmFacts() {
    navigate(ConfirmFactsNavKey())
}
