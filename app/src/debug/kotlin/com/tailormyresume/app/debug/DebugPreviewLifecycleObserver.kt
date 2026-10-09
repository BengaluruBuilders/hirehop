package com.tailormyresume.app.debug

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver

internal class DebugPreviewLifecycleObserver(
    private val activity: ComponentActivity,
    private val lifecycle: DebugPreviewLifecycle,
    private val opened: () -> Boolean,
    private val closePreview: () -> Unit,
) : DefaultLifecycleObserver
