package com.hirehop.feature.settings.impl.yourdata

import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.JobApplication

enum class YourDataStage { IDLE, PREPARING, READY }

enum class YourDataLedgerKind { PROFILE, APPLICATIONS, PURCHASES, UPLOADED_RESUME }

enum class YourDataLedgerAction { VIEW, CORRECT }

enum class YourDataLedgerUnit { FACTS, FILES, NONE }

enum class YourDataExportStepKind { PROFILE_FACTS, APPLICATIONS, PACKING }

enum class YourDataDestination { PROFILE_VIEW, PROFILE_CORRECT, APPLICATIONS_VIEW, PURCHASES_VIEW, SHARE_SHEET }

data class YourDataLedgerItem(
    val applicationId: String,
    val title: String,
    val company: String,
)

data class YourDataLedgerRow(
    val kind: YourDataLedgerKind,
    val count: Int,
    val unit: YourDataLedgerUnit,
    val confirmedFactCount: Int = 0,
    val userStatedFactCount: Int = 0,
    val purchaseCount: Int = 0,
    val purchasedCreditCount: Int = 0,
    val actions: List<YourDataLedgerAction> = emptyList(),
    val items: List<YourDataLedgerItem> = emptyList(),
)

data class YourDataExportStep(
    val kind: YourDataExportStepKind,
    val isDone: Boolean,
    val isCurrent: Boolean,
    val applicationCount: Int,
)

data class YourDataDeleteTarget(
    val applicationId: String,
    val title: String,
    val company: String,
)

data class YourDataUiState(
    val ledger: List<YourDataLedgerRow> = emptyList(),
    val stage: YourDataStage = YourDataStage.IDLE,
    val steps: List<YourDataExportStep> = emptyList(),
    val exportFileName: String? = null,
    val isOffline: Boolean = false,
    val deleteTarget: YourDataDeleteTarget? = null,
    val destination: YourDataDestination? = null,
    val profileFactCount: Int = 0,
) {
    val canDeleteApplications: Boolean get() = deleteTarget != null
    val isExporting: Boolean get() = stage == YourDataStage.PREPARING
}

sealed interface YourDataAction {
    data object DownloadTapped : YourDataAction

    data object ShareTapped : YourDataAction

    data class LedgerActionTapped(
        val kind: YourDataLedgerKind,
        val action: YourDataLedgerAction,
    ) : YourDataAction

    data class DeleteRequested(val applicationId: String) : YourDataAction

    data object DeleteConfirmed : YourDataAction

    data object DeleteDismissed : YourDataAction

    data class DestinationSelected(val destination: YourDataDestination) : YourDataAction

    data object DestinationConsumed : YourDataAction
}

fun yourDataIsOffline(scenario: DebugScenario): Boolean = scenario == DebugScenario.OFFLINE

fun yourDataLedger(
    profile: CandidateProfile?,
    applications: List<JobApplication>,
    entitlement: PurchaseEntitlement?,
): List<YourDataLedgerRow> = listOf(
    profileLedgerRow(profile = profile),
    applicationsLedgerRow(applications = applications),
    purchasesLedgerRow(entitlement = entitlement),
    uploadedResumeLedgerRow(),
)

fun yourDataExportSteps(
    currentIndex: Int,
    applicationCount: Int,
): List<YourDataExportStep> = YourDataExportStepKind.entries.mapIndexed { index, kind ->
    YourDataExportStep(
        kind = kind,
        isDone = index < currentIndex,
        isCurrent = index == currentIndex,
        applicationCount = applicationCount,
    )
}

fun yourDataExportFileName(fullName: String?): String {
    val slug = fullName
        .orEmpty()
        .split(' ', '\t', '\n')
        .filter { part -> part.isNotBlank() }
        .joinToString(separator = "-")
    return if (slug.isEmpty()) {
        HIREHOP_DATA_FILE_FALLBACK
    } else {
        "$HIREHOP_DATA_FILE_PREFIX$slug.zip"
    }
}

private fun profileLedgerRow(profile: CandidateProfile?): YourDataLedgerRow {
    val entries = profile?.entries.orEmpty()
    val confirmed = entries.count { entry -> entry.isConfirmed }
    return YourDataLedgerRow(
        kind = YourDataLedgerKind.PROFILE,
        count = entries.size,
        unit = YourDataLedgerUnit.FACTS,
        confirmedFactCount = confirmed,
        userStatedFactCount = entries.size - confirmed,
        actions = listOf(YourDataLedgerAction.VIEW, YourDataLedgerAction.CORRECT),
    )
}

private fun applicationsLedgerRow(applications: List<JobApplication>): YourDataLedgerRow =
    YourDataLedgerRow(
        kind = YourDataLedgerKind.APPLICATIONS,
        count = applications.size,
        unit = YourDataLedgerUnit.NONE,
        actions = listOf(YourDataLedgerAction.VIEW),
        items = applications.map { application ->
            YourDataLedgerItem(
                applicationId = application.id,
                title = application.job.title,
                company = application.job.company,
            )
        },
    )

private fun purchasesLedgerRow(entitlement: PurchaseEntitlement?): YourDataLedgerRow {
    val purchaseCount = entitlement?.pendingPackIds?.size ?: 0
    return YourDataLedgerRow(
        kind = YourDataLedgerKind.PURCHASES,
        count = purchaseCount,
        unit = YourDataLedgerUnit.NONE,
        purchaseCount = purchaseCount,
        purchasedCreditCount = entitlement?.purchasedCredits ?: 0,
        actions = listOf(YourDataLedgerAction.VIEW),
    )
}

private fun uploadedResumeLedgerRow(): YourDataLedgerRow = YourDataLedgerRow(
    kind = YourDataLedgerKind.UPLOADED_RESUME,
    count = 0,
    unit = YourDataLedgerUnit.FILES,
)

private const val HIREHOP_DATA_FILE_PREFIX = "HireHop-data_"
private const val HIREHOP_DATA_FILE_FALLBACK = "HireHop-data.zip"
