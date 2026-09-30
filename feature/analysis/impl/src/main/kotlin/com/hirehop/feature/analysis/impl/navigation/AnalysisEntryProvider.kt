package com.hirehop.feature.analysis.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.analysis.api.navigation.AnalysisNavKey
import com.hirehop.feature.analysis.impl.AnalysisScreen

fun EntryProviderScope<NavKey>.analysisEntry(navigator: Navigator) {
    entry<AnalysisNavKey> {
        AnalysisScreen(onBackClick = navigator::goBack)
    }
}
