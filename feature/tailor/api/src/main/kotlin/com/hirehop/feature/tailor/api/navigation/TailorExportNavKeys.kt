package com.hirehop.feature.tailor.api.navigation

import androidx.navigation3.runtime.NavKey
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class ExportPreviewNavKey(
    val applicationId: String,
    val format: String = "pdf",
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class PackPurchaseNavKey(
    val applicationId: String,
    val packId: String = "application_pack_5",
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

@Serializable
data class ExportedNavKey(
    val applicationId: String,
    val format: String = "pdf",
    val scenario: DebugScenario = DebugScenario.defaultValue,
    val spentFreeCredit: Boolean = true,
) : NavKey

@Serializable
data class CreditsNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

fun Navigator.navigateToExportPreview(applicationId: String, format: String = "pdf") {
    navigate(ExportPreviewNavKey(applicationId = applicationId, format = format))
}

fun Navigator.navigateToPackPurchase(applicationId: String, packId: String = "application_pack_5") {
    navigate(PackPurchaseNavKey(applicationId = applicationId, packId = packId))
}

fun Navigator.navigateToExported(applicationId: String, format: String = "pdf") {
    navigate(ExportedNavKey(applicationId = applicationId, format = format))
}

fun Navigator.navigateToCredits() {
    navigate(CreditsNavKey())
}
