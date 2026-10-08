package com.tailormyresume.feature.tailor.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.navigation.Navigator
import com.tailormyresume.feature.profile.api.navigation.FactEditorNavKey
import com.tailormyresume.feature.tailor.api.navigation.BulletReviewNavKey
import com.tailormyresume.feature.tailor.api.navigation.CoverLetterNavKey
import com.tailormyresume.feature.tailor.api.navigation.CreditsNavKey
import com.tailormyresume.feature.tailor.api.navigation.ExportPreviewNavKey
import com.tailormyresume.feature.tailor.api.navigation.ExportedNavKey
import com.tailormyresume.feature.tailor.api.navigation.PackPurchaseNavKey
import com.tailormyresume.feature.tailor.api.navigation.PrepQuestionsNavKey
import com.tailormyresume.feature.tailor.api.navigation.ShareLastExportNavKey
import com.tailormyresume.feature.tailor.api.navigation.TailorNavKey
import com.tailormyresume.feature.tailor.impl.TailorRoute
import com.tailormyresume.feature.tailor.impl.TailorViewModel
import com.tailormyresume.feature.tailor.impl.coverletter.CoverLetterRoute
import com.tailormyresume.feature.tailor.impl.credits.CreditsRoute
import com.tailormyresume.feature.tailor.impl.exported.ExportedRoute
import com.tailormyresume.feature.tailor.impl.exported.ShareLastExportRoute
import com.tailormyresume.feature.tailor.impl.exported.finishExport
import com.tailormyresume.feature.tailor.impl.exported.openApplicationWorkspace
import com.tailormyresume.feature.tailor.impl.exportpreview.ExportPreviewRoute
import com.tailormyresume.feature.tailor.impl.packpurchase.PackPurchaseRoute
import com.tailormyresume.feature.tailor.impl.prepquestions.PrepQuestionsRoute

fun EntryProviderScope<NavKey>.tailorEntry(navigator: Navigator) {
    entry<TailorNavKey> { key ->
        TailorRoute(
            onBackClick = { navigator.goBack() },
            onPreviewExport = { navigator.navigate(ExportPreviewNavKey(applicationId = key.applicationId)) },
            onEditFact = { entryId, entryType -> navigator.navigate(FactEditorNavKey(entryId, entryType)) },
            viewModel = hiltViewModel<TailorViewModel, TailorViewModel.Factory>(
                key = key.applicationId,
            ) { factory ->
                factory.create(key.applicationId, key.scenario)
            },
        )
    }
    entry<BulletReviewNavKey> { key ->
        TailorRoute(
            onBackClick = { navigator.goBack() },
            onPreviewExport = { navigator.navigate(ExportPreviewNavKey(applicationId = key.applicationId)) },
            onEditFact = { entryId, entryType -> navigator.navigate(FactEditorNavKey(entryId, entryType)) },
            viewModel = hiltViewModel<TailorViewModel, TailorViewModel.Factory>(
                key = key.applicationId,
            ) { factory ->
                factory.create(key.applicationId, key.scenario)
            },
            initialBulletId = key.bulletId,
            onBulletSheetClosed = { navigator.goBack() },
        )
    }
    entry<CoverLetterNavKey> { key ->
        CoverLetterRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
            onSkipLetter = { navigator.goBack() },
            onPreviewExport = { navigator.navigate(ExportPreviewNavKey(applicationId = key.applicationId)) },
            onPrepQuestions = { navigator.navigate(PrepQuestionsNavKey(applicationId = key.applicationId)) },
        )
    }
    entry<PrepQuestionsNavKey> { key ->
        PrepQuestionsRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
            onOpenPrepPlan = { navigator.openApplicationWorkspace(key.applicationId) },
            onEditFact = { entryId, entryType -> navigator.navigate(FactEditorNavKey(entryId, entryType)) },
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
            onBuyCredits = { format ->
                navigator.navigate(
                    PackPurchaseNavKey(
                        applicationId = key.applicationId,
                        startExportOnReturn = true,
                        format = format.name.lowercase(),
                    ),
                )
            },
        )
    }
    entry<ExportedNavKey> { key ->
        ExportedRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
            onDone = { navigator.finishExport(key.applicationId) },
            onGetPrepQuestions = { navigator.navigate(PrepQuestionsNavKey(applicationId = key.applicationId)) },
            onWriteCoverLetter = { navigator.navigate(CoverLetterNavKey(applicationId = key.applicationId)) },
        )
    }
    entry<ShareLastExportNavKey> { key ->
        ShareLastExportRoute(
            key = key,
            onDone = { navigator.goBack() },
            onFileMissing = { navigator.replace(ExportPreviewNavKey(applicationId = key.applicationId)) },
        )
    }
    entry<PackPurchaseNavKey> { key ->
        PackPurchaseRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
            onOpenCredits = { navigator.navigate(CreditsNavKey()) },
        )
    }
    entry<CreditsNavKey> { key ->
        CreditsRoute(
            key = key,
            onNavigateBack = { navigator.goBack() },
            onGetPack = { navigator.navigate(PackPurchaseNavKey(applicationId = "")) },
        )
    }
}
