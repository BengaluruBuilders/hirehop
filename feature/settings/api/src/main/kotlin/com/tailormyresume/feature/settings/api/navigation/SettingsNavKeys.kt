package com.tailormyresume.feature.settings.api.navigation

import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class SettingsNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class YourDataNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class DeleteAccountNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data object AccountDeletedNavKey : NavKey

val DefaultSettingsNavKey = SettingsNavKey()

fun Navigator.navigateToSettings() {
    navigate(SettingsNavKey())
}

fun Navigator.navigateToYourData() {
    navigate(YourDataNavKey())
}

fun Navigator.navigateToDeleteAccount() {
    navigate(DeleteAccountNavKey())
}
