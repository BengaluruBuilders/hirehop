package com.tailormyresume.app.ui

import com.tailormyresume.core.domain.onboarding.StartDestination

sealed interface AppRootState {
    data object Loading : AppRootState

    data object FirstRun : AppRootState

    data object Main : AppRootState

    data class Ready(val start: StartDestination, val accountId: String?) : AppRootState
}
