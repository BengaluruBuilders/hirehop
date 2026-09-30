package com.hirehop.feature.applications.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.analysis.api.navigation.navigateToAnalysis
import com.hirehop.feature.applications.api.navigation.ApplicationsNavKey
import com.hirehop.feature.applications.impl.ApplicationsScreen

fun EntryProviderScope<NavKey>.applicationsEntry(navigator: Navigator) {
    entry<ApplicationsNavKey> {
        ApplicationsScreen(onAddApplicationClick = navigator::navigateToAnalysis)
    }
}
