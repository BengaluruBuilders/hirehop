package com.hirehop.feature.applications.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.analysis.api.navigation.navigateToAnalysis
import com.hirehop.feature.applications.api.navigation.ApplicationDetailNavKey
import com.hirehop.feature.applications.api.navigation.ApplicationsNavKey
import com.hirehop.feature.applications.api.navigation.navigateToApplicationDetail
import com.hirehop.feature.applications.impl.ApplicationDetailRoute
import com.hirehop.feature.applications.impl.ApplicationDetailViewModel
import com.hirehop.feature.applications.impl.ApplicationsRoute
import com.hirehop.feature.tailor.api.navigation.navigateToTailor

fun EntryProviderScope<NavKey>.applicationsEntry(navigator: Navigator) {
    entry<ApplicationsNavKey> {
        ApplicationsRoute(
            onApplicationClick = navigator::navigateToApplicationDetail,
            onNewApplicationClick = navigator::navigateToAnalysis,
        )
    }
}

fun EntryProviderScope<NavKey>.applicationDetailEntry(navigator: Navigator) {
    entry<ApplicationDetailNavKey> { key ->
        val applicationId = key.applicationId
        ApplicationDetailRoute(
            onBackClick = navigator::goBack,
            onReviewResumeClick = navigator::navigateToTailor,
            viewModel = hiltViewModel<ApplicationDetailViewModel, ApplicationDetailViewModel.Factory>(
                key = applicationId,
            ) { factory ->
                factory.create(applicationId)
            },
        )
    }
}
