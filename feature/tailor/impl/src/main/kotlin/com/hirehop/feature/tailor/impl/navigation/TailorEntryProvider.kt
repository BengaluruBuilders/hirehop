package com.hirehop.feature.tailor.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.tailor.api.navigation.TailorNavKey
import com.hirehop.feature.tailor.impl.TailorRoute
import com.hirehop.feature.tailor.impl.TailorViewModel

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
}
