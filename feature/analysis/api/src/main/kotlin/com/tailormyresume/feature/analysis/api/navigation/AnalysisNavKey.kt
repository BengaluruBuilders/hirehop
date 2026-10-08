package com.tailormyresume.feature.analysis.api.navigation

import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class AnalysisNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

val DefaultAnalysisNavKey = AnalysisNavKey()

fun Navigator.navigateToAnalysis() {
    navigate(DefaultAnalysisNavKey)
}
