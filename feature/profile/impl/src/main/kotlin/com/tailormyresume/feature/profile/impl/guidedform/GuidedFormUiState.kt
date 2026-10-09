package com.tailormyresume.feature.profile.impl.guidedform

import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.feature.profile.impl.ProfileExit

enum class GuidedArrival { NORMAL, FROM_SCANNED_PDF }

enum class GuidedFieldProblem { REQUIRED, END_BEFORE_START, TOO_LONG, INVALID_EMAIL, INVALID_PHONE }

enum class GuidedMessage { LOAD_FAILED, SAVE_FAILED }

enum class ExperienceChoice { YES, NO }

data class GuidedSaved(
    val completedSteps: Int,
    val totalSteps: Int,
    val entryIds: List<String>,
)

sealed interface GuidedNavigation {
    data class Evidence(val category: String) : GuidedNavigation

    data class Exit(val exit: ProfileExit) : GuidedNavigation
}

data class GuidedFormUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val isSaving: Boolean = false,
    val arrival: GuidedArrival = GuidedArrival.NORMAL,
    val showIntro: Boolean = false,
    val stepIndex: Int = 0,
    val values: Map<GuidedField, String> = emptyMap(),
    val skills: List<String> = emptyList(),
    val fieldProblems: Map<GuidedField, GuidedFieldProblem> = emptyMap(),
    val filedEntries: List<ProfileEntry> = emptyList(),
    val completedSteps: Set<GuidedStep> = emptySet(),
    val stepEntryIds: Map<GuidedStep, List<String>> = emptyMap(),
    val experienceChoice: ExperienceChoice? = null,
    val saved: GuidedSaved? = null,
    val message: GuidedMessage? = null,
    val navigation: GuidedNavigation? = null,
) {
    val step: GuidedStep get() = guidedStepAt(stepIndex)
    val isLastStep: Boolean get() = stepIndex == GUIDED_STEPS.lastIndex
    val isFirstStep: Boolean get() = stepIndex == 0
    val createdEntryIds: List<String> get() = GUIDED_STEPS.flatMap { stepEntryIds[it].orEmpty() }
}

fun guidedFormStateFor(
    scenario: DebugScenario,
    startStep: String,
    resumedFromScan: Boolean,
): GuidedFormUiState {
    val arrival = if (scenario == DebugScenario.SCANNED || resumedFromScan) {
        GuidedArrival.FROM_SCANNED_PDF
    } else {
        GuidedArrival.NORMAL
    }
    return GuidedFormUiState(
        isLoading = scenario == DebugScenario.LOADING || scenario == DebugScenario.DELETING,
        isOffline = scenario == DebugScenario.OFFLINE,
        arrival = arrival,
        showIntro = arrival == GuidedArrival.FROM_SCANNED_PDF,
        stepIndex = guidedStepIndexOf(startStep),
        message = if (scenario == DebugScenario.ERROR) GuidedMessage.LOAD_FAILED else null,
    )
}
