package com.hirehop.feature.applications.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.applications.api.navigation.ApplicationDetailNavKey
import com.hirehop.feature.applications.api.navigation.ApplicationsNavKey
import com.hirehop.feature.applications.api.navigation.navigateToApplicationDetail
import com.hirehop.feature.applications.impl.ApplicationDetailRoute
import com.hirehop.feature.applications.impl.ApplicationDetailViewModel
import com.hirehop.feature.applications.impl.ApplicationsRoute
import com.hirehop.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import com.hirehop.feature.tailor.api.navigation.CoverLetterNavKey
import com.hirehop.feature.tailor.api.navigation.CreditsNavKey
import com.hirehop.feature.tailor.api.navigation.ExportPreviewNavKey
import com.hirehop.feature.tailor.api.navigation.PrepQuestionsNavKey
import com.hirehop.feature.tailor.api.navigation.navigateToShareLastExport
import com.hirehop.feature.tailor.api.navigation.navigateToTailor

fun EntryProviderScope<NavKey>.applicationsEntry(navigator: Navigator) {
    entry<ApplicationsNavKey> { key ->
        ApplicationsRoute(
            onApplicationClick = navigator::navigateToApplicationDetail,
            onPasteJobClick = { navigator.navigate(PasteJobDescriptionNavKey()) },
            onCreditsClick = { navigator.navigate(CreditsNavKey()) },
            scenario = key.scenario,
        )
    }
}

fun EntryProviderScope<NavKey>.applicationDetailEntry(navigator: Navigator) {
    entry<ApplicationDetailNavKey> { key ->
        val applicationId = key.applicationId
        ApplicationDetailRoute(
            viewModel = hiltViewModel<ApplicationDetailViewModel, ApplicationDetailViewModel.Factory>(
                key = applicationId,
            ) { factory ->
                factory.create(applicationId)
            },
            onBackClick = navigator::goBack,
            scenario = key.scenario,
            onReviewResumeClick = navigator::navigateToTailor,
            onPrepQuestionsClick = { navigator.navigate(PrepQuestionsNavKey(applicationId = applicationId)) },
            onCoverLetterClick = { navigator.navigate(CoverLetterNavKey(applicationId = applicationId)) },
            onExportPreviewClick = { navigator.navigate(ExportPreviewNavKey(applicationId = applicationId)) },
            onShareExportClick = { navigator.navigateToShareLastExport(applicationId) },
        )
    }
}
