package com.hirehop.feature.settings.impl.deleteaccount

import com.hirehop.core.domain.account.AccountDeletionCounts
import com.hirehop.core.domain.account.AccountDeletionStep
import com.hirehop.core.model.DebugScenario

enum class DeleteAccountStage {
    DEFAULT,
    DELETING,
    DONE,
    ERROR,
}

enum class DeleteAccountDestination {
    WELCOME,
    YOUR_DATA,
}

data class DeleteAccountStepState(
    val step: AccountDeletionStep,
    val isDone: Boolean,
    val isCurrent: Boolean,
    val isPending: Boolean,
)

data class DeleteAccountUiState(
    val stage: DeleteAccountStage = DeleteAccountStage.DEFAULT,
    val counts: AccountDeletionCounts = AccountDeletionCounts(
        profileFacts = 0,
        applications = 0,
        unusedCredits = 0,
    ),
    val steps: List<DeleteAccountStepState> = deleteAccountSteps(currentIndex = 0),
    val isOffline: Boolean = false,
    val isDataIntact: Boolean = true,
    val destination: DeleteAccountDestination? = null,
) {
    val isBackEnabled: Boolean
        get() = stage != DeleteAccountStage.DELETING

    val isDeleteEnabled: Boolean
        get() = !isOffline && stage != DeleteAccountStage.DELETING && stage != DeleteAccountStage.DONE

    val isKeepEnabled: Boolean
        get() = stage != DeleteAccountStage.DELETING && stage != DeleteAccountStage.DONE

    val showsMainStage: Boolean
        get() = stage == DeleteAccountStage.DEFAULT || stage == DeleteAccountStage.ERROR
}

fun deleteAccountSteps(currentIndex: Int): List<DeleteAccountStepState> =
    AccountDeletionStep.entries.mapIndexed { index, step ->
        DeleteAccountStepState(
            step = step,
            isDone = index < currentIndex,
            isCurrent = index == currentIndex,
            isPending = index > currentIndex,
        )
    }

fun deleteAccountStateFor(
    scenario: DebugScenario,
    counts: AccountDeletionCounts = AccountDeletionCounts(
        profileFacts = 0,
        applications = 0,
        unusedCredits = 0,
    ),
    isOffline: Boolean = false,
): DeleteAccountUiState = when (scenario) {
    DebugScenario.DELETING -> DeleteAccountUiState(
        stage = DeleteAccountStage.DELETING,
        counts = counts,
        steps = deleteAccountSteps(currentIndex = 1),
        isOffline = isOffline,
    )

    DebugScenario.SUCCESS -> DeleteAccountUiState(
        stage = DeleteAccountStage.DONE,
        counts = counts,
        isOffline = isOffline,
    )

    DebugScenario.ERROR -> DeleteAccountUiState(
        stage = DeleteAccountStage.ERROR,
        counts = counts,
        isOffline = isOffline,
    )

    DebugScenario.OFFLINE -> DeleteAccountUiState(
        stage = DeleteAccountStage.DEFAULT,
        counts = counts,
        isOffline = true,
    )

    else -> DeleteAccountUiState(counts = counts, isOffline = isOffline)
}

fun deleteAccountIsOffline(scenario: DebugScenario): Boolean = scenario == DebugScenario.OFFLINE
