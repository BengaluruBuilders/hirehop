package com.tailormyresume.feature.tailor.impl.exported

import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.ExportFormat
import java.io.File

internal enum class ExportedFileAction { SHARE, OPEN }

internal data class ExportedFileRequest(
    val file: File,
    val format: ExportFormat,
    val action: ExportedFileAction,
    val jobTitle: String,
    val jobCompany: String,
)

internal enum class ExportedStage {
    LOADING,
    READY,
    NO_APPLICATION,
    NO_FILE,
}

internal enum class ExportedCreditSource { FREE, PAID }

internal data class ExportedUiState(
    val stage: ExportedStage = ExportedStage.LOADING,
    val format: ExportFormat = ExportFormat.PDF,
    val jobTitle: String = "",
    val jobCompany: String = "",
    val fileName: String = "",
    val fileOnDevice: Boolean = false,
    val pageCount: Int? = null,
    val templateName: String? = null,
    val creditsKnown: Boolean = false,
    val creditSource: ExportedCreditSource = ExportedCreditSource.FREE,
    val creditsLeft: Int = 0,
    val creditsNeverExpire: Boolean = false,
    val status: ApplicationStatus = ApplicationStatus.SAVED,
    val markedOn: String? = null,
    val statusSheetOpen: Boolean = false,
    val undoStatus: ApplicationStatus? = null,
    val fileRequest: ExportedFileRequest? = null,
) {
    val usesFreeCredit: Boolean get() = creditSource == ExportedCreditSource.FREE

    val creditsBefore: Int get() = creditsLeft + 1

    val canUseFile: Boolean get() = fileOnDevice

    val asksForStatus: Boolean get() = status == ApplicationStatus.SAVED
}

internal fun exportedIsStatic(scenario: DebugScenario): Boolean = scenario == DebugScenario.LOADING

internal fun exportedHasNoApplication(scenario: DebugScenario): Boolean = scenario == DebugScenario.EMPTY
