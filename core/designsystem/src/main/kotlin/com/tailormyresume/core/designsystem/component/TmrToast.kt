package com.tailormyresume.core.designsystem.component

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

enum class TmrToastResult { Dismissed, ActionPerformed }

@Stable
class TmrToastState internal constructor() {
    internal val host = SnackbarHostState()

    suspend fun show(
        message: String,
        actionLabel: String? = null,
        duration: TmrSnackbarDuration = TmrSnackbarDuration.Standard,
    ): TmrToastResult {
        val result = host.showTmrSnackbar(message = message, actionLabel = actionLabel, duration = duration)
        return if (result == SnackbarResult.ActionPerformed) TmrToastResult.ActionPerformed else TmrToastResult.Dismissed
    }

    fun dismiss() {
        host.currentSnackbarData?.dismiss()
    }
}

@Composable
fun rememberTmrToastState(): TmrToastState = remember { TmrToastState() }

@Composable
fun TmrToastHost(
    state: TmrToastState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = state.host, modifier = modifier) { data ->
        TmrSnackbar(snackbarData = data)
    }
}
