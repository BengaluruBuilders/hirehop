package com.tailormyresume.feature.tailor.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.navigation.Navigator
import com.tailormyresume.feature.tailor.api.navigation.EditResumeNavKey
import com.tailormyresume.feature.tailor.api.navigation.ExportedNavKey
import com.tailormyresume.feature.tailor.api.navigation.TailorFailedNavKey
import com.tailormyresume.feature.tailor.api.navigation.TailoredNavKey
import com.tailormyresume.feature.tailor.api.navigation.TailoringNavKey

fun EntryProviderScope<NavKey>.tailorEntry(navigator: Navigator) {
    entry<TailoringNavKey> { key -> NavKeyPlaceholder(key) }
    entry<TailorFailedNavKey> { key -> NavKeyPlaceholder(key) }
    entry<TailoredNavKey> { key -> NavKeyPlaceholder(key) }
    entry<EditResumeNavKey> { key -> NavKeyPlaceholder(key) }
    entry<ExportedNavKey> { key -> NavKeyPlaceholder(key) }
}
