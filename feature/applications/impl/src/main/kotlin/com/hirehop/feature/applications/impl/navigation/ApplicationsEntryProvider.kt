package com.hirehop.feature.applications.impl.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.domain.account.AccountCreditBalance
import com.hirehop.core.domain.account.AccountCreditLine
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.analysis.api.navigation.navigateToAnalysis
import com.hirehop.feature.applications.api.navigation.ApplicationDetailNavKey
import com.hirehop.feature.applications.api.navigation.ApplicationsNavKey
import com.hirehop.feature.applications.api.navigation.navigateToApplicationDetail
import com.hirehop.feature.applications.impl.ApplicationCreditLine
import com.hirehop.feature.applications.impl.ApplicationCreditUnit
import com.hirehop.feature.applications.impl.ApplicationDetailRoute
import com.hirehop.feature.applications.impl.ApplicationDetailViewModel
import com.hirehop.feature.applications.impl.ApplicationsRoute
import com.hirehop.feature.tailor.api.navigation.CoverLetterNavKey
import com.hirehop.feature.tailor.api.navigation.ExportPreviewNavKey
import com.hirehop.feature.tailor.api.navigation.PrepQuestionsNavKey
import com.hirehop.feature.tailor.api.navigation.navigateToTailor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface ApplicationsCreditEntryPoint {
    fun accountCreditBalance(): AccountCreditBalance
}

fun EntryProviderScope<NavKey>.applicationsEntry(navigator: Navigator) {
    entry<ApplicationsNavKey> { key ->
        val context = LocalContext.current
        val balance = remember(context) {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                ApplicationsCreditEntryPoint::class.java,
            ).accountCreditBalance()
        }
        val credits by produceState<ApplicationCreditLine?>(initialValue = null, balance) {
            value = runCatching { balance.creditLine() }.getOrNull()?.asApplicationLine()
        }
        ApplicationsRoute(
            onApplicationClick = navigator::navigateToApplicationDetail,
            onNewApplicationClick = navigator::navigateToAnalysis,
            scenario = key.scenario,
            credits = credits,
        )
    }
}

fun EntryProviderScope<NavKey>.applicationDetailEntry(navigator: Navigator) {
    entry<ApplicationDetailNavKey> { key ->
        val applicationId = key.applicationId
        ApplicationDetailRoute(
            onBackClick = navigator::goBack,
            onReviewResumeClick = navigator::navigateToTailor,
            onPrepQuestionsClick = { navigator.navigate(PrepQuestionsNavKey(applicationId = applicationId)) },
            onCoverLetterClick = { navigator.navigate(CoverLetterNavKey(applicationId = applicationId)) },
            onExportPreviewClick = { navigator.navigate(ExportPreviewNavKey(applicationId = applicationId)) },
            viewModel = hiltViewModel<ApplicationDetailViewModel, ApplicationDetailViewModel.Factory>(
                key = applicationId,
            ) { factory ->
                factory.create(applicationId)
            },
        )
    }
}

private fun AccountCreditLine.asApplicationLine(): ApplicationCreditLine =
    when {
        freeCredits > 0 -> ApplicationCreditLine(amount = freeCredits, unit = ApplicationCreditUnit.Free)
        else -> ApplicationCreditLine(amount = purchasedCredits, unit = ApplicationCreditUnit.Left)
    }
