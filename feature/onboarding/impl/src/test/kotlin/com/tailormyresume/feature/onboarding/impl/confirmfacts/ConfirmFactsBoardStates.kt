package com.tailormyresume.feature.onboarding.impl.confirmfacts

import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario

internal fun ConfirmFactsUiState.withSaveFailed(): ConfirmFactsUiState = copy(hasSaveFailed = true)

internal fun emptySectionState(profile: CandidateProfile): ConfirmFactsUiState =
    ConfirmFactsScenarioMapper.withProfile(
        state = ConfirmFactsScenarioMapper.seed(DebugScenario.DEFAULT),
        profile = profile,
        scenario = DebugScenario.DEFAULT,
    )
