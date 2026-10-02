package com.hirehop.core.designsystem.component

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

enum class HhToastResult { Dismissed, ActionPerformed }

@Stable
class HhToastState internal constructor() {
    internal val host = SnackbarHostState()

    suspend fun show(
        message: String,
        actionLabel: String? = null,
        duration: HhSnackbarDuration = HhSnackbarDuration.Standard,
    ): HhToastResult {
        val result = host.showHhSnackbar(message = message, actionLabel = actionLabel, duration = duration)
        return if (result == SnackbarResult.ActionPerformed) HhToastResult.ActionPerformed else HhToastResult.Dismissed
    }

    fun dismiss() {
        host.currentSnackbarData?.dismiss()
    }
}

@Composable
fun rememberHhToastState(): HhToastState = remember { HhToastState() }

@Composable
fun HhToastHost(
    state: HhToastState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = state.host, modifier = modifier) { data ->
        HhSnackbar(snackbarData = data)
    }
}
