package com.hirehop.feature.profile.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import com.hirehop.feature.profile.api.navigation.FactEditorNavKey
import com.hirehop.feature.profile.api.navigation.FactEvidenceNavKey
import com.hirehop.feature.profile.api.navigation.GuidedProfileFormNavKey
import com.hirehop.feature.profile.api.navigation.ProfileNavKey
import com.hirehop.feature.profile.api.navigation.navigateToFactEvidence
import com.hirehop.feature.profile.impl.ProfileRoute
import com.hirehop.feature.profile.impl.evidencepath.EvidencePathRoute
import com.hirehop.feature.profile.impl.guidedform.GuidedFormRoute

fun EntryProviderScope<NavKey>.profileEntry(navigator: Navigator) {
    entry<ProfileNavKey> { key ->
        ProfileRoute(
            scenario = key.scenario,
            onOpenFact = { entryId -> navigator.navigate(FactEditorNavKey(entryId = entryId)) },
            onAddEvidence = { navigator.navigateToFactEvidence() },
            onBuildStepByStep = {
                navigator.navigate(GuidedProfileFormNavKey())
            },
        )
    }
    entry<GuidedProfileFormNavKey> { key ->
        GuidedFormRoute(
            key = key,
            onNavigateToEvidence = { category -> navigator.navigateToFactEvidence(category) },
        )
    }
    entry<FactEvidenceNavKey> { key ->
        EvidencePathRoute(
            key = key,
            onGoToProfile = { navigator.goBack() },
        )
    }
}
