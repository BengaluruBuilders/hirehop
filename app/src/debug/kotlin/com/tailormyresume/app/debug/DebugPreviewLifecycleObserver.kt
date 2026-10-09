package com.tailormyresume.app.debug

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

internal class DebugPreviewLifecycleObserver(
    private val activity: ComponentActivity,
    private val lifecycle: DebugPreviewLifecycle,
    private val opened: () -> Boolean,
    private val closePreview: () -> Unit,
) : DefaultLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) = lifecycle.onCreate()

    override fun onStart(owner: LifecycleOwner) = lifecycle.onStart(opened())

    override fun onStop(owner: LifecycleOwner) {
        val close = lifecycle.onStop(opened(), activity.isFinishing, activity.isChangingConfigurations)
        if (close) closePreview()
    }
}
