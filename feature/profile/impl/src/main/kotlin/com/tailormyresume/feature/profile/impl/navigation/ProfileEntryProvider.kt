package com.tailormyresume.feature.profile.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.navigation.Navigator
import com.tailormyresume.feature.onboarding.api.navigation.navigateToImportResume
import com.tailormyresume.feature.profile.api.navigation.DefaultProfileNavKey
import com.tailormyresume.feature.profile.api.navigation.FactEditorNavKey
import com.tailormyresume.feature.profile.api.navigation.FactEvidenceNavKey
import com.tailormyresume.feature.profile.api.navigation.GuidedProfileFormNavKey
import com.tailormyresume.feature.profile.api.navigation.ProfileNavKey
import com.tailormyresume.feature.profile.api.navigation.navigateToFactEditor
import com.tailormyresume.feature.profile.api.navigation.navigateToFactEvidence
import com.tailormyresume.feature.profile.api.navigation.navigateToGuidedProfileForm
import com.tailormyresume.feature.profile.impl.ProfileExit
import com.tailormyresume.feature.profile.impl.ProfileNavigation
import com.tailormyresume.feature.profile.impl.ProfileRoute
import com.tailormyresume.feature.profile.impl.evidencepath.EvidencePathNavigation
import com.tailormyresume.feature.profile.impl.evidencepath.EvidencePathRoute
import com.tailormyresume.feature.profile.impl.facteditor.FactEditorRoute
import com.tailormyresume.feature.profile.impl.guidedform.GuidedFormNavigation
import com.tailormyresume.feature.profile.impl.guidedform.GuidedFormRoute

fun EntryProviderScope<NavKey>.profileEntry(navigator: Navigator) {
    entry<ProfileNavKey> { key ->
        ProfileRoute(
            scenario = key.scenario,
            navigation = ProfileNavigation(
                onOpenFact = { entryId -> navigator.navigateToFactEditor(entryId) },
                onAddFact = { entryType -> navigator.navigateToFactEditor(entryId = null, entryType = entryType) },
                onAddEvidence = { navigator.navigateToFactEvidence() },
                onBuildStepByStep = { navigator.navigateToGuidedProfileForm() },
                onImportResume = { navigator.navigateToImportResume() },
            ),
        )
    }
    entry<FactEditorNavKey> { key ->
        FactEditorRoute(key = key, onClose = { navigator.goBack() })
    }
    entry<GuidedProfileFormNavKey> { key ->
        GuidedFormRoute(
            key = key,
            navigation = GuidedFormNavigation(
                onBack = { navigator.goBack() },
                onOpenEvidence = { category -> navigator.navigateToFactEvidence(category) },
                onAddJob = { navigator.navigateToFactEditor(entryId = null, entryType = EXPERIENCE_TYPE) },
                onExit = { exit -> navigator.leave(exit) },
            ),
        )
    }
    entry<FactEvidenceNavKey> { key ->
        EvidencePathRoute(
            key = key,
            navigation = EvidencePathNavigation(
                onBack = { navigator.goBack() },
                onEditFact = { entryId, entryType -> navigator.navigateToFactEditor(entryId, entryType) },
                onExit = { exit -> navigator.leave(exit) },
            ),
        )
    }
}

private fun Navigator.leave(exit: ProfileExit) {
    when (exit) {
        ProfileExit.Profile -> navigate(DefaultProfileNavKey)
        is ProfileExit.Step -> navigate(exit.key)
    }
}

private const val EXPERIENCE_TYPE = "experience"
