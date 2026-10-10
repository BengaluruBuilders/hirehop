package com.tailormyresume.feature.analysis.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.navigation.Navigator
import com.tailormyresume.feature.analysis.api.navigation.JobLinkNavKey
import com.tailormyresume.feature.analysis.api.navigation.JobNavKey
import com.tailormyresume.feature.analysis.api.navigation.JobResultNavKey
import com.tailormyresume.feature.analysis.api.navigation.QuickQuestionNavKey

fun EntryProviderScope<NavKey>.analysisEntry(navigator: Navigator) {
    entry<JobNavKey> { key -> NavKeyPlaceholder(key) }
    entry<JobLinkNavKey> { key -> NavKeyPlaceholder(key) }
    entry<JobResultNavKey> { key -> NavKeyPlaceholder(key) }
    entry<QuickQuestionNavKey> { key -> NavKeyPlaceholder(key) }
}
