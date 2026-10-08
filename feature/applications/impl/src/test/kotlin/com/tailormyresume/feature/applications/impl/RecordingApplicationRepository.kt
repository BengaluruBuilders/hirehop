package com.tailormyresume.feature.applications.impl

import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.core.testing.repository.TestApplicationRepository
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
