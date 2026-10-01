package com.hirehop.feature.analysis.api.navigation

import androidx.navigation3.runtime.NavKey
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class AnalysisNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

val DefaultAnalysisNavKey = AnalysisNavKey()

fun Navigator.navigateToAnalysis() {
    navigate(DefaultAnalysisNavKey)
}
