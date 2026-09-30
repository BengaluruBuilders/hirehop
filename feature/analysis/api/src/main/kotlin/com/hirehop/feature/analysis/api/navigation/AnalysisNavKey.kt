package com.hirehop.feature.analysis.api.navigation

import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object AnalysisNavKey : NavKey

fun Navigator.navigateToAnalysis() {
    navigate(AnalysisNavKey)
}
