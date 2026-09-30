package com.hirehop.feature.tailor.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.tailor.api.navigation.BulletReviewNavKey
import com.hirehop.feature.tailor.api.navigation.CoverLetterNavKey
import com.hirehop.feature.tailor.api.navigation.CreditsNavKey
import com.hirehop.feature.tailor.api.navigation.ExportPreviewNavKey
import com.hirehop.feature.tailor.api.navigation.ExportedNavKey
import com.hirehop.feature.tailor.api.navigation.PackPurchaseNavKey
import com.hirehop.feature.tailor.api.navigation.PrepQuestionsNavKey
import com.hirehop.feature.tailor.api.navigation.TailorNavKey
import com.hirehop.feature.tailor.impl.TailorRoute
import com.hirehop.feature.tailor.impl.TailorViewModel
import com.hirehop.feature.tailor.impl.coverletter.CoverLetterRoute
import com.hirehop.feature.tailor.impl.credits.CreditsRoute
import com.hirehop.feature.tailor.impl.exported.ExportedRoute
import com.hirehop.feature.tailor.impl.exportpreview.ExportPreviewRoute
import com.hirehop.feature.tailor.impl.packpurchase.PackPurchaseRoute
import com.hirehop.feature.tailor.impl.prepquestions.PrepQuestionsRoute

fun EntryProviderScope<NavKey>.tailorEntry(navigator: Navigator) {
    entry<TailorNavKey> { key ->
        val applicationId = key.applicationId
        TailorRoute(
            onBackClick = { navigator.goBack() },
            viewModel = hiltViewModel<TailorViewModel, TailorViewModel.Factory>(
                key = applicationId,
            ) { factory ->
                factory.create(applicationId)
            },
        )
    }
    entry<BulletReviewNavKey> { key ->
        val applicationId = key.applicationId
        TailorRoute(
            onBackClick = { navigator.goBack() },
            viewModel = hiltViewModel<TailorViewModel, TailorViewModel.Factory>(
                key = applicationId,
            ) { factory ->
                factory.create(applicationId)
            },
        )
    }
    entry<CoverLetterNavKey> { key ->
        CoverLetterRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
            onSkipLetter = { navigator.goBack() },
        )
    }
    entry<PrepQuestionsNavKey> { key ->
        PrepQuestionsRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
        )
    }
    entry<ExportPreviewNavKey> { key ->
        ExportPreviewRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
            onExported = { format, spentFreeCredit ->
                navigator.navigate(
                    ExportedNavKey(
                        applicationId = key.applicationId,
                        format = format.name.lowercase(),
                        spentFreeCredit = spentFreeCredit,
                    ),
                )
            },
            onBuyCredits = {
                navigator.navigate(PackPurchaseNavKey(applicationId = key.applicationId))
            },
        )
    }
    entry<ExportedNavKey> { key ->
        ExportedRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
        )
    }
    entry<PackPurchaseNavKey> { key ->
        PackPurchaseRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
        )
    }
    entry<CreditsNavKey> { key ->
        CreditsRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
        )
    }
}
