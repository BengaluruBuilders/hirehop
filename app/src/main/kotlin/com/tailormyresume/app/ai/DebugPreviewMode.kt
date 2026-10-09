package com.tailormyresume.app.ai

import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugPreviewMode @Inject constructor() {
    private val open = AtomicBoolean(false)

    var active: Boolean
        get() = open.get()
        set(value) = open.set(value)
}
