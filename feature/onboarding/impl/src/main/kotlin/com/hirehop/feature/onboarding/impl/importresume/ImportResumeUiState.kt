package com.hirehop.feature.onboarding.impl.importresume

import com.hirehop.core.domain.fact.FactLineRenderer
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry

enum class ImportStage {
    Idle,
    Picking,
    Parsing,
    Success,
    NoFactsFound,
    ScannedNoText,
    Unsupported,
    Empty,
    TooLarge,
    Failed,
}

data class ImportedFactUi(
    val id: String,
    val category: EntryCategory,
    val line: String,
)

data class ImportResumeUiState(
    val stage: ImportStage = ImportStage.Idle,
    val isOffline: Boolean = false,
    val fileName: String = "",
    val byteSize: Long = 0L,
    val readStepIndex: Int = 0,
    val facts: List<ImportedFactUi> = emptyList(),
    val skillCount: Int = 0,
    val isQueued: Boolean = false,
) {
    val isPicking: Boolean get() = stage == ImportStage.Picking
    val isParsing: Boolean get() = stage == ImportStage.Parsing
    val isBusy: Boolean get() = isPicking || isParsing
    val isStop: Boolean get() = stage in STOP_STAGES
    val isSuccess: Boolean get() = stage == ImportStage.Success
    val canPick: Boolean get() = !isBusy
    val factCount: Int get() = facts.size + skillCount
}

private val STOP_STAGES = setOf(
    ImportStage.NoFactsFound,
    ImportStage.ScannedNoText,
    ImportStage.Empty,
    ImportStage.TooLarge,
    ImportStage.Failed,
)

object ImportResumeScenarioMapper {

    fun seed(scenario: DebugScenario): ImportResumeUiState = when (scenario) {
        DebugScenario.LOADING -> ImportResumeUiState(stage = ImportStage.Picking)
        DebugScenario.OFFLINE -> ImportResumeUiState(
            isOffline = true,
            fileName = SAMPLE_FILE_NAME,
            byteSize = SAMPLE_FILE_SIZE,
            isQueued = true,
        )
        DebugScenario.PARTIAL -> ImportResumeUiState(
            stage = ImportStage.Parsing,
            fileName = SAMPLE_FILE_NAME,
            byteSize = SAMPLE_FILE_SIZE,
            readStepIndex = 1,
            facts = listOf(SAMPLE_FACTS.first()),
        )
        DebugScenario.SUCCESS, DebugScenario.IMPORTED -> ImportResumeUiState(
            stage = ImportStage.Success,
            fileName = SAMPLE_FILE_NAME,
            byteSize = SAMPLE_FILE_SIZE,
            readStepIndex = READ_STEP_COUNT,
            facts = SAMPLE_FACTS,
        )
        DebugScenario.SCANNED -> ImportResumeUiState(
            stage = ImportStage.ScannedNoText,
            fileName = SAMPLE_FILE_NAME,
            byteSize = SAMPLE_FILE_SIZE,
            readStepIndex = READ_STEP_COUNT,
        )
        DebugScenario.ERROR -> ImportResumeUiState(
            stage = ImportStage.Failed,
            fileName = SAMPLE_FILE_NAME,
            byteSize = SAMPLE_FILE_SIZE,
        )
        else -> ImportResumeUiState()
    }
}

const val READ_STEP_COUNT: Int = 3

private const val SAMPLE_FILE_NAME = "Resume_2026.pdf"
private const val SAMPLE_FILE_SIZE = 212L * 1024L

private val SAMPLE_FACTS: List<ImportedFactUi> = listOf(
    ImportedFactUi(
        id = "U-01",
        category = EntryCategory.EDUCATION,
        line = FactLineRenderer.render(
            entry(
                id = "U-01",
                category = EntryCategory.EDUCATION,
                title = "B.Tech Computer Science",
                organization = "Example Institute of Technology",
                startDate = "2022",
                endDate = "2026",
                detail = "CGPA 8.1",
            ),
        ),
    ),
    ImportedFactUi(
        id = "I-01",
        category = EntryCategory.EXPERIENCE,
        line = FactLineRenderer.render(
            entry(
                id = "I-01",
                category = EntryCategory.EXPERIENCE,
                title = "Data intern, Kiran Agro Exports",
                organization = "Nashik",
                startDate = "May 2025",
                endDate = "Jul 2025",
                detail = "Cleaned 12,000 rows of sales data in Excel and built weekly pivot reports.",
            ),
        ),
    ),
    ImportedFactUi(
        id = "C-01",
        category = EntryCategory.PROJECT,
        line = FactLineRenderer.render(
            entry(
                id = "C-01",
                category = EntryCategory.PROJECT,
                title = "Placement Stats Dashboard",
                organization = "Power BI, Excel",
                startDate = "2024",
                endDate = "2024",
                detail = "Three batches of placement data for the college placement cell.",
            ),
        ),
    ),
)

private fun entry(
    id: String,
    category: EntryCategory,
    title: String,
    organization: String,
    startDate: String,
    endDate: String,
    detail: String,
): ProfileEntry = ProfileEntry(
    id = id,
    category = category,
    title = title,
    organization = organization,
    startDate = startDate,
    endDate = endDate,
    bullets = listOf(EvidenceBullet(id = "$id-b1", text = detail)),
    source = FactSource.IMPORTED,
    isConfirmed = false,
)
