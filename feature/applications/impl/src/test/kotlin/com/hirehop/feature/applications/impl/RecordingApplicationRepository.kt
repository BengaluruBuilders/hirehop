package com.hirehop.feature.applications.impl

import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.model.JobApplication
import com.hirehop.core.testing.repository.TestApplicationRepository
import kotlinx.coroutines.CompletableDeferred

internal class RecordingApplicationRepository(
    private val delegate: TestApplicationRepository = TestApplicationRepository(),
) : ApplicationRepository by delegate {

    val notesWrites = mutableListOf<String>()
    var notesWriteGate: CompletableDeferred<Unit>? = null

    fun sendApplications(applications: List<JobApplication>) =
        delegate.sendApplications(applications)

    override suspend fun updateNotes(id: String, notes: String) {
        notesWrites += notes
        notesWriteGate?.await()
        delegate.updateNotes(id, notes)
    }
}
