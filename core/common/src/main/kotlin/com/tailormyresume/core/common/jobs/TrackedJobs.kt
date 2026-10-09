package com.tailormyresume.core.common.jobs

import kotlinx.coroutines.Job
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackedJobs @Inject constructor() {
    private val jobs = ConcurrentHashMap.newKeySet<Job>()

    @Volatile
    private var tracking = false

    fun startTracking() {
        tracking = true
    }

    fun track(job: Job) {
        if (!tracking) return
        jobs.add(job)
        job.invokeOnCompletion { jobs.remove(job) }
    }

    fun cancelTracked() {
        tracking = false
        jobs.toList().forEach(Job::cancel)
        jobs.clear()
    }
}
