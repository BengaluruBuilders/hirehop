package com.tailormyresume.app.ai

import com.tailormyresume.core.common.jobs.TrackedJobs
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugPreviewMode @Inject constructor(private val trackedJobs: TrackedJobs) {
    constructor() : this(TrackedJobs())

    private val open = AtomicBoolean(false)

    var active: Boolean
        get() = open.get()
        set(value) {
            val wasOpen = open.getAndSet(value)
            if (value && !wasOpen) trackedJobs.startTracking()
            if (!value && wasOpen) trackedJobs.cancelTracked()
        }
}
