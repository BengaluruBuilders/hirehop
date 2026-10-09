package com.tailormyresume.app.debug

import com.tailormyresume.app.ai.DebugPreviewMode

internal class DebugPreviewLifecycle(
    private val previewMode: DebugPreviewMode,
    private val forcePayment: () -> Unit,
    private val releasePayment: () -> Unit,
) {
    fun onCreate() {
        previewMode.active = false
        releasePayment()
    }

    fun onStart(opened: Boolean) {
        previewMode.active = opened
        if (opened) forcePayment()
    }

    fun onStop(opened: Boolean, finishing: Boolean, changingConfigurations: Boolean): Boolean {
        if (finishing) return opened && !changingConfigurations
        previewMode.active = false
        releasePayment()
        return false
    }
}
