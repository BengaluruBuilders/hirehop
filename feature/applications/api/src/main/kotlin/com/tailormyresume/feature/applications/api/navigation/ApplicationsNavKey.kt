package com.tailormyresume.feature.applications.api.navigation

import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationsNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class ApplicationDetailNavKey(
    val applicationId: String,
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

val DefaultApplicationsNavKey = ApplicationsNavKey()

fun Navigator.navigateToApplicationDetail(applicationId: String) {
    navigate(ApplicationDetailNavKey(applicationId))
}
