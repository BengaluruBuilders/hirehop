package com.tailormyresume.feature.profile.api.navigation

import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.navigation.Navigator
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
    val returnsToProfile: Boolean = false,
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class FactEvidenceNavKey(
    val category: String = "",
    val returnsToProfile: Boolean = false,
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

fun Navigator.navigateToFactEditor(entryId: String?, entryType: String = "project") {
    navigate(FactEditorNavKey(entryId = entryId, entryType = entryType))
}

fun Navigator.navigateToGuidedProfileForm(resumedFromScan: Boolean = false) {
    navigate(GuidedProfileFormNavKey(resumedFromScan = resumedFromScan))
}

fun Navigator.navigateToFactEvidence(category: String = "") {
    navigate(FactEvidenceNavKey(category = category))
}
