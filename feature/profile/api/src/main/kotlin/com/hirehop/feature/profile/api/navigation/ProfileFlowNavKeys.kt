package com.hirehop.feature.profile.api.navigation

import androidx.navigation3.runtime.NavKey
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class FactEditorNavKey(
    val entryId: String?,
    val entryType: String = "project",
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class GuidedProfileFormNavKey(
    val startStep: String = "contact",
    val resumedFromScan: Boolean = false,
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class FactEvidenceNavKey(
    val category: String = "projects",
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

fun Navigator.navigateToFactEditor(entryId: String?) {
    navigate(FactEditorNavKey(entryId = entryId))
}

fun Navigator.navigateToGuidedProfileForm(resumedFromScan: Boolean = false) {
    navigate(GuidedProfileFormNavKey(resumedFromScan = resumedFromScan))
}

fun Navigator.navigateToFactEvidence(category: String = "projects") {
    navigate(FactEvidenceNavKey(category = category))
}
