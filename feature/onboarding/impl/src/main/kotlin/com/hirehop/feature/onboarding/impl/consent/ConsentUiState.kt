package com.hirehop.feature.onboarding.impl.consent

import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.DebugScenario
import kotlin.time.Instant

data class ConsentPurposeState(
    val purpose: ConsentPurpose,
    val isAcknowledged: Boolean = false,
)

data class ConsentUiState(
    val entries: List<ConsentPurposeState> = consentPurposeStates(),
    val isSaving: Boolean = false,
    val isDeclined: Boolean = false,
    val isReadOnly: Boolean = false,
    val uploadFailed: Boolean = false,
    val agreedAt: Instant? = null,
    val nextStep: OnboardingStep? = null,
) {
    val acknowledgedCount: Int get() = entries.count { it.isAcknowledged }
    val isEveryPurposeAcknowledged: Boolean get() = entries.isNotEmpty() && entries.all { it.isAcknowledged }
    val canAgree: Boolean get() = isEveryPurposeAcknowledged && !isSaving && !isReadOnly
    val showAgreementActions: Boolean get() = !isDeclined && !isReadOnly
}

fun consentPurposeStates(): List<ConsentPurposeState> = ConsentPurpose.entries.map { ConsentPurposeState(purpose = it) }

fun consentStateFor(scenario: DebugScenario, readOnly: Boolean = false): ConsentUiState = when {
    readOnly -> ConsentUiState(isReadOnly = true)
    scenario == DebugScenario.LOADING -> ConsentUiState(isSaving = true)
    scenario == DebugScenario.EMPTY -> ConsentUiState(isDeclined = true)
    scenario == DebugScenario.SUCCESS -> ConsentUiState(
        entries = consentPurposeStates().map { it.copy(isAcknowledged = true) },
    )
    scenario == DebugScenario.PARTIAL -> ConsentUiState(
        entries = consentPurposeStates().mapIndexed { index, entry -> entry.copy(isAcknowledged = index < 2) },
    )
    else -> ConsentUiState()
}

fun ConsentUiState.isAcknowledged(purpose: ConsentPurpose): Boolean =
    entries.firstOrNull { it.purpose == purpose }?.isAcknowledged == true
