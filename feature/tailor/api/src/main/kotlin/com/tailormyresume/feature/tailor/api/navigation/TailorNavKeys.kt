package com.tailormyresume.feature.tailor.api.navigation

import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class TailoringNavKey(
    val applicationId: String,
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class TailorFailedNavKey(
    val applicationId: String,
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class TailoredNavKey(
    val applicationId: String,
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class EditResumeNavKey(
    val applicationId: String,
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class ExportedNavKey(
    val applicationId: String,
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

fun Navigator.navigateToTailoring(applicationId: String) {
    navigate(TailoringNavKey(applicationId))
}

fun Navigator.navigateToExported(applicationId: String) {
    navigate(ExportedNavKey(applicationId))
}
