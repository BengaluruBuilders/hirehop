package com.hirehop.feature.onboarding.impl.consent

import com.hirehop.core.model.DebugScenario

enum class ConsentPurpose { READ_AND_BUILD, ANALYSE_ON_DEVICE, KEEP_CONFIRMED_FACTS }

data class ConsentPurposeState(
    val purpose: ConsentPurpose,
    val isAcknowledged: Boolean = false,
)

data class ConsentUiState(
    val entries: List<ConsentPurposeState> = consentPurposeStates(),
    val isSaving: Boolean = false,
    val isOffline: Boolean = false,
    val isDeclined: Boolean = false,
) {
    val acknowledgedCount: Int get() = entries.count { it.isAcknowledged }
    val isEveryPurposeAcknowledged: Boolean get() = entries.isNotEmpty() && entries.all { it.isAcknowledged }
    val canAgree: Boolean get() = isEveryPurposeAcknowledged && !isSaving
    val showAgreementActions: Boolean get() = !isDeclined
}

fun consentPurposeStates(): List<ConsentPurposeState> = ConsentPurpose.entries.map { ConsentPurposeState(purpose = it) }

fun consentStateFor(scenario: DebugScenario): ConsentUiState = when (scenario) {
    DebugScenario.DEFAULT,
    DebugScenario.ERROR,
    DebugScenario.PARTIAL,
    DebugScenario.USER_STATED,
    DebugScenario.SCANNED,
    DebugScenario.IMPORTED,
    DebugScenario.FULLY_CONFIRMED,
    DebugScenario.PARTLY_CONFIRMED,
    DebugScenario.DELETING,
    DebugScenario.EXPORTING,
    DebugScenario.PURCHASED,
    DebugScenario.PENDING,
    DebugScenario.CANCELLED,
    DebugScenario.RESTORED,
    -> ConsentUiState()

    DebugScenario.LOADING -> ConsentUiState(isSaving = true)

    DebugScenario.EMPTY -> ConsentUiState(isDeclined = true)

    DebugScenario.OFFLINE -> ConsentUiState(isOffline = true)

    DebugScenario.SUCCESS -> ConsentUiState(
        entries = consentPurposeStates().map { it.copy(isAcknowledged = true) },
    )
}

fun ConsentUiState.isAcknowledged(purpose: ConsentPurpose): Boolean =
    entries.firstOrNull { it.purpose == purpose }?.isAcknowledged == true
