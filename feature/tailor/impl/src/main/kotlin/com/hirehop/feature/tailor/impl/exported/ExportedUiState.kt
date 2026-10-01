package com.hirehop.feature.tailor.impl.exported

import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.DebugScenario
import com.hirehop.feature.tailor.impl.exportpreview.ExportFormat
import java.io.File

internal data class ExportedShareRequest(
    val file: File,
    val format: ExportFormat,
    val jobTitle: String,
    val jobCompany: String,
)

internal enum class ExportedStage {
    IDLE,
    READY,
    NO_APPLICATION,
    NO_FILE,
}

internal enum class ExportedCreditSource { FREE, PAID }

internal enum class ExportedShareState { IDLE, REQUESTED }

internal data class ExportedUiState(
    val stage: ExportedStage = ExportedStage.IDLE,
    val format: ExportFormat = ExportFormat.PDF,
    val jobTitle: String = "",
    val jobCompany: String = "",
    val fileName: String = "",
    val fileOnDevice: Boolean = false,
    val creditsKnown: Boolean = false,
    val creditSource: ExportedCreditSource = ExportedCreditSource.FREE,
    val creditsBefore: Int = 0,
    val creditsLeft: Int = 0,
    val creditsNeverExpire: Boolean = false,
    val status: ApplicationStatus = ApplicationStatus.SAVED,
    val statusSheetOpen: Boolean = false,
    val statusJustSet: Boolean = false,
    val shareState: ExportedShareState = ExportedShareState.IDLE,
    val shareRequest: ExportedShareRequest? = null,
) {
    val hasJob: Boolean get() = jobTitle.isNotBlank() || jobCompany.isNotBlank()

    val usesFreeCredit: Boolean get() = creditSource == ExportedCreditSource.FREE

    val creditsSpent: Int get() = (creditsBefore - creditsLeft).coerceAtLeast(0)

    val canShare: Boolean get() = fileOnDevice

    val asksForStatus: Boolean get() = status == ApplicationStatus.SAVED

    val showsStatusChip: Boolean get() = !asksForStatus
}

internal fun exportedIsStatic(scenario: DebugScenario): Boolean = scenario == DebugScenario.LOADING

internal fun exportedHasNoApplication(scenario: DebugScenario): Boolean =
    scenario == DebugScenario.EMPTY
