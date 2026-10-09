package com.tailormyresume.feature.profile.impl.evidencepath

import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.feature.profile.impl.ProfileExit

enum class EvidenceFieldProblem { REQUIRED, TOO_LONG }

enum class EvidenceMessage { LOAD_FAILED, SAVE_FAILED }

data class EvidenceFactCard(
    val category: EvidenceCategory,
    val entry: ProfileEntry,
)

data class EvidenceSkipNote(
    val category: EvidenceCategory,
    val questionNumber: Int,
)

sealed interface EvidenceNavigation {
    data class Exit(val exit: ProfileExit) : EvidenceNavigation
}

data class EvidencePathUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val isSaving: Boolean = false,
    val category: EvidenceCategory? = null,
    val questionIndex: Int = 0,
    val answer: String = "",
    val problem: EvidenceFieldProblem? = null,
    val cards: List<EvidenceFactCard> = emptyList(),
    val visited: Set<EvidenceCategory> = emptySet(),
    val categoryOrder: List<EvidenceCategory> = EVIDENCE_CATEGORIES,
    val skipNote: EvidenceSkipNote? = null,
    val isDone: Boolean = false,
    val stamped: EvidenceFactCard? = null,
    val message: EvidenceMessage? = null,
    val navigation: EvidenceNavigation? = null,
) {
    val isPicker: Boolean get() = category == null && !isDone
    val questionNumber: Int get() = questionIndex + 1
    val questionTotal: Int get() = category?.questionCount ?: 0
    val categoryCards: List<EvidenceFactCard> get() = cards.filter { it.category == category }
    val projectName: String? get() = null
    val canSave: Boolean get() = answer.isNotBlank() && !isSaving
}

fun evidencePathStateFor(
    scenario: DebugScenario,
    category: String,
): EvidencePathUiState = EvidencePathUiState(
    isLoading = scenario == DebugScenario.LOADING || scenario == DebugScenario.DELETING,
    isOffline = scenario == DebugScenario.OFFLINE,
    category = evidenceCategoryOrNull(category),
    message = if (scenario == DebugScenario.ERROR) EvidenceMessage.LOAD_FAILED else null,
)
