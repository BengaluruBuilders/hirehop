package com.tailormyresume.app.ui

sealed interface AppRootState {
    data object Loading : AppRootState

    data object FirstRun : AppRootState

    data object Main : AppRootState
}
