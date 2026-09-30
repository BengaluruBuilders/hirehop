package com.hirehop.feature.tailor.impl.exportpreview

import com.hirehop.core.model.DebugScenario
import com.hirehop.feature.tailor.impl.document.ResumeDocument

internal enum class ExportPreviewStage {
    IDLE,
    RENDERING,
    PREVIEW_READY,
    PREVIEW_FAILED,
    EXPORTING,
    EXPORT_SUCCEEDED,
    EXPORT_FAILED,
    OFFLINE,
    NO_DOCUMENT,
    NO_CREDIT,
}

internal data class ExportPreviewSheet(
    val name: String,
    val contactLine: String,
    val headline: String,
    val skills: List<String>,
    val sections: List<ExportPreviewSection>,
) {
    val lineCount: Int
        get() = 1 + listOf(contactLine, headline).count { line -> line.isNotBlank() } +
            skills.size + sections.sumOf { section -> section.entries.sumOf { entry -> 1 + entry.bullets.size } + 1 }
}

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

internal data class ExportPreviewUiState(
    val stage: ExportPreviewStage = ExportPreviewStage.IDLE,
    val format: ExportFormat = ExportFormat.PDF,
    val jobTitle: String = "",
    val jobCompany: String = "",
    val sheet: ExportPreviewSheet? = null,
    val fileName: String = "",
    val isOffline: Boolean = false,
    val exportedFormat: ExportFormat? = null,
    val creditsLeft: Int = 0,
    val isFreeCredit: Boolean = true,
    val packPrice: String = "",
    val creditKnown: Boolean = false,
) {
    val hasSheet: Boolean get() = sheet != null

    val hasCredit: Boolean get() = creditsLeft > 0

    val needsCredits: Boolean get() = creditKnown && !hasCredit

    val canExport: Boolean
        get() = stage == ExportPreviewStage.PREVIEW_READY && hasSheet && !isOffline && !needsCredits

    val showsPackPrice: Boolean
        get() = stage == ExportPreviewStage.NO_CREDIT && packPrice.isNotEmpty()

    val exportFailed: Boolean get() = stage == ExportPreviewStage.EXPORT_FAILED
}

internal fun exportPreviewStageFor(scenario: DebugScenario): ExportPreviewStage = when (scenario) {
    DebugScenario.LOADING -> ExportPreviewStage.RENDERING
    DebugScenario.ERROR -> ExportPreviewStage.PREVIEW_FAILED
    DebugScenario.EMPTY -> ExportPreviewStage.NO_DOCUMENT
    DebugScenario.OFFLINE -> ExportPreviewStage.OFFLINE
    DebugScenario.EXPORTING -> ExportPreviewStage.EXPORTING
    else -> ExportPreviewStage.IDLE
}

internal fun exportPreviewIsStatic(scenario: DebugScenario): Boolean =
    scenario == DebugScenario.LOADING ||
        scenario == DebugScenario.ERROR ||
        scenario == DebugScenario.EMPTY

internal fun exportPreviewIsOffline(scenario: DebugScenario): Boolean = scenario == DebugScenario.OFFLINE

internal fun exportPreviewExportsOnEntry(scenario: DebugScenario): Boolean =
    scenario == DebugScenario.EXPORTING

internal fun exportPreviewSheetOf(document: ResumeDocument): ExportPreviewSheet = ExportPreviewSheet(
    name = document.name,
    contactLine = document.contactLine,
    headline = document.headline,
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
