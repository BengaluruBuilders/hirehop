package com.tailormyresume.app.ai

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.common.jobs.TrackedJobs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Test

class DebugPreviewJobsTest {

    private val trackedJobs = TrackedJobs()
    private val previewMode = DebugPreviewMode(trackedJobs)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

    @After
    fun tearDown() = scope.cancel()

    private fun trackedPendingJob(): Job = scope.launch { awaitCancellation() }.also(trackedJobs::track)

    @Test
    fun aJobStartedInAPreviewIsCancelledWhenThePreviewEnds() {
        previewMode.active = true
        val job = trackedPendingJob()

        previewMode.active = false

        assertThat(job.isCancelled).isTrue()
    }

    @Test
    fun aJobStartedOutsideAPreviewSurvivesAPreview() {
        val job = trackedPendingJob()

        previewMode.active = true
        previewMode.active = false

        assertThat(job.isActive).isTrue()
    }

    @Test
    fun aJobStaysActiveWhileThePreviewIsOpen() {
        previewMode.active = true
        val job = trackedPendingJob()

        assertThat(job.isActive).isTrue()
    }

    @Test
    fun aJobFromAnEarlierPreviewDoesNotFollowALaterOne() {
        previewMode.active = true
        val first = trackedPendingJob()
        previewMode.active = false
        previewMode.active = true
        val second = trackedPendingJob()

        previewMode.active = false

        assertThat(first.isCancelled).isTrue()
        assertThat(second.isCancelled).isTrue()
    }

    @Test
    fun endingAPreviewThatNeverOpenedCancelsNothing() {
        val job = trackedPendingJob()

        previewMode.active = false

        assertThat(job.isActive).isTrue()
    }
}
