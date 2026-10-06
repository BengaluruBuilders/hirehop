package com.hirehop.feature.tailor.impl.exportpreview

import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.ExportFormat
import com.hirehop.feature.tailor.impl.document.ResumeDocument

internal enum class ExportPreviewStage {
    RENDERING,
    PREVIEW_READY,
    EXPORTING,
    PREVIEW_FAILED,
    EXPORT_FAILED,
    NO_DOCUMENT,
}

internal data class ExportPreviewSheet(
    val name: String,
    val contactLine: String,
    val headline: String,
    val skillsHeading: String,
    val skills: List<String>,
    val sections: List<ExportPreviewSection>,
)

internal data class ExportPreviewSection(
    val heading: String,
    val entries: List<ExportPreviewEntry>,
)

internal data class ExportPreviewEntry(
    val title: String,
    val organization: String,
    val dateRange: String,
    val bullets: List<String>,
)

internal sealed interface ExportPreviewNavigation {
    data class Exported(val format: ExportFormat, val spentFreeCredit: Boolean) : ExportPreviewNavigation

    data object BuyCredits : ExportPreviewNavigation
}

internal data class ExportPreviewUiState(
    val stage: ExportPreviewStage = ExportPreviewStage.RENDERING,
    val format: ExportFormat = ExportFormat.PDF,
    val jobTitle: String = "",
    val jobCompany: String = "",
    val sheet: ExportPreviewSheet? = null,
    val fileName: String = "",
    val isOffline: Boolean = false,
    val creditsKnown: Boolean = false,
    val freeCredits: Int = 0,
    val purchasedCredits: Int = 0,
    val isFreeBeta: Boolean = false,
    val navigation: ExportPreviewNavigation? = null,
) {
    val totalCredits: Int get() = freeCredits + purchasedCredits

    val needsCredits: Boolean get() = creditsKnown && !isFreeBeta && totalCredits == 0

    val canExport: Boolean
        get() = stage == ExportPreviewStage.PREVIEW_READY && sheet != null && !isOffline

    val showsDownload: Boolean
        get() = stage == ExportPreviewStage.PREVIEW_READY
}

internal fun exportPreviewIsStatic(scenario: DebugScenario): Boolean =
    scenario == DebugScenario.LOADING || scenario == DebugScenario.ERROR || scenario == DebugScenario.EMPTY

internal fun exportPreviewIsOffline(scenario: DebugScenario): Boolean = scenario == DebugScenario.OFFLINE

internal fun exportPreviewStageFor(scenario: DebugScenario): ExportPreviewStage = when (scenario) {
    DebugScenario.LOADING -> ExportPreviewStage.RENDERING
    DebugScenario.ERROR -> ExportPreviewStage.PREVIEW_FAILED
    DebugScenario.EMPTY -> ExportPreviewStage.NO_DOCUMENT
    DebugScenario.EXPORTING -> ExportPreviewStage.EXPORTING
    else -> ExportPreviewStage.RENDERING
}

internal fun exportPreviewSheetOf(document: ResumeDocument): ExportPreviewSheet = ExportPreviewSheet(
    name = document.name,
    contactLine = document.contactLine,
    headline = document.headline,
    skillsHeading = document.skillsHeading,
    skills = document.skills,
    sections = document.sections.map { section ->
        ExportPreviewSection(
            heading = section.heading,
            entries = section.entries.map { entry ->
                ExportPreviewEntry(
                    title = entry.title,
                    organization = entry.organization,
                    dateRange = entry.dateRange,
                    bullets = entry.bullets,
                )
            },
        )
    },
)
