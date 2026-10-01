package com.hirehop.feature.analysis.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.analysis.api.navigation.AnalysisNavKey
import com.hirehop.feature.analysis.impl.AnalysisRoute
import com.hirehop.feature.profile.api.navigation.DefaultProfileNavKey
import com.hirehop.feature.tailor.api.navigation.navigateToTailor

fun EntryProviderScope<NavKey>.analysisEntry(navigator: Navigator) {
    entry<AnalysisNavKey> {
        AnalysisRoute(
            onBackClick = { navigator.goBack() },
            onOpenProfile = { navigator.navigate(DefaultProfileNavKey) },
            onOpenTailor = navigator::navigateToTailor,
        )
    }
}
