package com.tailormyresume.feature.onboarding.impl.confirmfacts

import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario

internal fun ConfirmFactsUiState.withSaveFailed(): ConfirmFactsUiState = this

internal fun emptySectionState(profile: CandidateProfile): ConfirmFactsUiState =
    ConfirmFactsScenarioMapper.withProfile(
        state = ConfirmFactsScenarioMapper.seed(DebugScenario.FULLY_CONFIRMED),
        profile = profile,
        scenario = DebugScenario.FULLY_CONFIRMED,
    )
