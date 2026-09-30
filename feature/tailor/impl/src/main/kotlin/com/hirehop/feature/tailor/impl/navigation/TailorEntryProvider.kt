package com.hirehop.feature.tailor.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.tailor.api.navigation.TailorNavKey
import com.hirehop.feature.tailor.impl.TailorScreen

fun EntryProviderScope<NavKey>.tailorEntry(navigator: Navigator) {
    entry<TailorNavKey> {
        TailorScreen(onBackClick = navigator::goBack)
    }
}
