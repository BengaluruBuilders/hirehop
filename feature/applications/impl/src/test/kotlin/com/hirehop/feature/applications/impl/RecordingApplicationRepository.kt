package com.hirehop.feature.applications.impl

import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.model.JobApplication
import com.hirehop.core.testing.repository.TestApplicationRepository

internal class RecordingApplicationRepository(
    private val delegate: TestApplicationRepository = TestApplicationRepository(),
) : ApplicationRepository by delegate {

    val notesWrites = mutableListOf<String>()

    fun sendApplications(applications: List<JobApplication>) =
        delegate.sendApplications(applications)

    override suspend fun updateNotes(id: String, notes: String) {
        notesWrites += notes
        delegate.updateNotes(id, notes)
    }
}
