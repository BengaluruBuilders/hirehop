package com.hirehop.feature.profile.impl.evidencepath

import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.ProfileEntry

enum class EvidenceFieldProblem { REQUIRED, END_BEFORE_START, TOO_LONG }

enum class EvidenceMessage { LOAD_FAILED, SAVED, OFFLINE_QUEUED, SAVE_REJECTED }

data class EvidenceFactCard(
    val category: EvidenceCategory,
    val entryCategory: EntryCategory,
    val line: String,
    val answer: String,
    val entry: ProfileEntry? = null,
)

data class EvidenceQuestion(
    val category: EvidenceCategory,
    val promptIndex: Int,
    val totalPrompts: Int,
    val answers: Map<EvidencePrompt, String>,
    val problems: Map<EvidencePrompt, EvidenceFieldProblem> = emptyMap(),
) {
    val prompt: EvidencePrompt get() = category.prompts()[promptIndex]
    val title: String get() = answers[EvidencePrompt.TITLE].orEmpty()
    val detail: String get() = answers[EvidencePrompt.DETAIL].orEmpty()
    val organization: String get() = answers[EvidencePrompt.ORGANIZATION].orEmpty()
    val isLastPrompt: Boolean get() = promptIndex == totalPrompts - 1
}

data class EvidenceDone(
    val addedCount: Int,
    val skippedCount: Int,
)

data class EvidenceStage(
    val done: EvidenceDone?,
    val question: EvidenceQuestion?,
)

data class EvidencePathUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val isSaving: Boolean = false,
    val startCategory: EvidenceCategory = EvidenceCategory.PROJECTS,
    val category: EvidenceCategory? = null,
    val question: EvidenceQuestion? = null,
    val cards: List<EvidenceFactCard> = emptyList(),
    val isSaveRejected: Boolean = false,
    val skipped: List<EvidenceCategory> = emptyList(),
    val addedCount: Int = 0,
    val done: EvidenceDone? = null,
    val message: EvidenceMessage? = null,
) {
    val isPicker: Boolean get() = category == null
}

fun evidencePathStateFor(
    scenario: DebugScenario,
    category: String,
): EvidencePathUiState {
    val start = evidenceCategoryOf(category)
    return when (scenario) {
        DebugScenario.LOADING, DebugScenario.DELETING -> EvidencePathUiState(isLoading = true, startCategory = start)

        DebugScenario.OFFLINE -> EvidencePathUiState(isOffline = true, startCategory = start)

        DebugScenario.ERROR -> EvidencePathUiState(
            startCategory = start,
            message = EvidenceMessage.LOAD_FAILED,
        )

        DebugScenario.EMPTY -> EvidencePathUiState(startCategory = start)

        else -> EvidencePathUiState(
            startCategory = start,
            category = start,
            question = EvidenceQuestion(
                category = start,
                promptIndex = 0,
                totalPrompts = start.prompts().size,
                answers = emptyMap(),
            ),
        )
    }
}
