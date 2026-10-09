package com.tailormyresume.core.common.jobs

import kotlinx.coroutines.Job
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackedJobs @Inject constructor() {
    fun startTracking() = Unit

    fun track(job: Job) = Unit

    fun cancelTracked() = Unit
}
