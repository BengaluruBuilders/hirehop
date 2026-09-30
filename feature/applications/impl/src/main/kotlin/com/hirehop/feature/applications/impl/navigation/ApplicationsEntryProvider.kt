package com.hirehop.feature.applications.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.analysis.api.navigation.AnalysisNavKey
import com.hirehop.feature.applications.api.navigation.ApplicationDetailNavKey
import com.hirehop.feature.applications.api.navigation.ApplicationsNavKey
import com.hirehop.feature.applications.impl.ApplicationDetailRoute
import com.hirehop.feature.applications.impl.ApplicationDetailViewModel
import com.hirehop.feature.applications.impl.ApplicationsRoute
import com.hirehop.feature.tailor.api.navigation.TailorNavKey

fun EntryProviderScope<NavKey>.applicationsEntry(navigator: Navigator) {
    entry<ApplicationsNavKey> {
        ApplicationsRoute(
            onApplicationClick = { applicationId ->
                navigator.navigate(ApplicationDetailNavKey(applicationId))
            },
            onNewApplicationClick = { navigator.navigate(AnalysisNavKey) },
        )
    }
}

fun EntryProviderScope<NavKey>.applicationDetailEntry(navigator: Navigator) {
    entry<ApplicationDetailNavKey> { key ->
        val applicationId = key.applicationId
        ApplicationDetailRoute(
            onBackClick = navigator::goBack,
            onReviewResumeClick = { navigator.navigate(TailorNavKey(it)) },
            viewModel = hiltViewModel<ApplicationDetailViewModel, ApplicationDetailViewModel.Factory>(
                key = applicationId,
            ) { factory ->
                factory.create(applicationId)
            },
        )
    }
}
