package com.hirehop.feature.profile.impl.guidedform

import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.ProfileEntry

enum class GuidedArrival { NORMAL, FROM_SCANNED_PDF }

enum class GuidedFieldProblem { REQUIRED, END_BEFORE_START, TOO_LONG }

enum class GuidedMessage { LOAD_FAILED, SAVED, OFFLINE_QUEUED, SAVE_REJECTED }

data class GuidedFactPreview(
    val category: EntryCategory,
    val line: String,
    val entry: ProfileEntry? = null,
)

data class GuidedSaved(
    val completedSteps: Int,
    val totalSteps: Int,
)

data class GuidedHandoff(
    val category: String,
)

data class GuidedFormUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val isSaving: Boolean = false,
    val arrival: GuidedArrival = GuidedArrival.NORMAL,
    val stepIndex: Int = 0,
    val values: Map<GuidedField, String> = emptyMap(),
    val fieldProblems: Map<GuidedField, GuidedFieldProblem> = emptyMap(),
    val previews: List<GuidedFactPreview> = emptyList(),
    val isSaveRejected: Boolean = false,
    val completedSteps: List<GuidedStep> = emptyList(),
    val saved: GuidedSaved? = null,
    val handoff: GuidedHandoff? = null,
    val message: GuidedMessage? = null,
) {
    val step: GuidedStep get() = guidedStepAt(stepIndex)
    val skills: List<String> get() = skillsOf(values)
    val isLastStep: Boolean get() = stepIndex == GUIDED_STEPS.lastIndex
    val isFirstStep: Boolean get() = stepIndex == 0
}

fun guidedFormStateFor(
    scenario: DebugScenario,
    startStep: String,
    resumedFromScan: Boolean,
): GuidedFormUiState = when (scenario) {
    DebugScenario.LOADING, DebugScenario.DELETING -> GuidedFormUiState(
        isLoading = true,
        stepIndex = guidedStepIndexOf(startStep),
        arrival = arrivalFor(scenario, resumedFromScan),
    )

    DebugScenario.OFFLINE -> GuidedFormUiState(
        isOffline = true,
        stepIndex = guidedStepIndexOf(startStep),
        arrival = arrivalFor(scenario, resumedFromScan),
    )

    DebugScenario.ERROR -> GuidedFormUiState(
        stepIndex = guidedStepIndexOf(startStep),
        arrival = arrivalFor(scenario, resumedFromScan),
        message = GuidedMessage.LOAD_FAILED,
    )

    else -> GuidedFormUiState(
        stepIndex = guidedStepIndexOf(startStep),
        arrival = arrivalFor(scenario, resumedFromScan),
    )
}

private fun arrivalFor(
    scenario: DebugScenario,
    resumedFromScan: Boolean,
): GuidedArrival = if (scenario == DebugScenario.SCANNED || resumedFromScan) {
    GuidedArrival.FROM_SCANNED_PDF
} else {
    GuidedArrival.NORMAL
}
