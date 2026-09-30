package com.hirehop.core.testing.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.testing.data.sampleApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TestApplicationRepositoryTest {

    private val repository = TestApplicationRepository()

    @Test
    fun observeApplications_emitsEmptyList_initially() = runTest {
        assertThat(repository.observeApplications().first()).isEmpty()
    }

    @Test
    fun upsertApplication_addsNewApplication() = runTest {
        repository.upsertApplication(sampleApplication)

        assertThat(repository.observeApplications().first()).containsExactly(sampleApplication)
    }

    @Test
    fun upsertApplication_replacesApplicationWithSameId() = runTest {
        val edited = sampleApplication.copy(notes = "Follow up on Monday")
        repository.upsertApplication(sampleApplication)

        repository.upsertApplication(edited)

        assertThat(repository.observeApplications().first()).containsExactly(edited)
    }

    @Test
    fun observeApplication_emitsMatchingApplication() = runTest {
        repository.upsertApplication(sampleApplication)

        repository.observeApplication(sampleApplication.id).test {
            assertThat(awaitItem()).isEqualTo(sampleApplication)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeApplication_emitsNull_whenIdIsUnknown() = runTest {
        repository.upsertApplication(sampleApplication)

        assertThat(repository.observeApplication("unknown-id").first()).isNull()
    }

    @Test
    fun updateStatus_changesOnlyStatus() = runTest {
        repository.upsertApplication(sampleApplication)

        repository.updateStatus(sampleApplication.id, ApplicationStatus.INTERVIEW)

        val updated = repository.observeApplication(sampleApplication.id).first()
        assertThat(updated).isEqualTo(sampleApplication.copy(status = ApplicationStatus.INTERVIEW))
    }

    @Test
    fun updateNotes_changesOnlyNotes() = runTest {
        repository.upsertApplication(sampleApplication)

        repository.updateNotes(sampleApplication.id, "Referred by a friend")

        val updated = repository.observeApplication(sampleApplication.id).first()
        assertThat(updated).isEqualTo(sampleApplication.copy(notes = "Referred by a friend"))
    }

    @Test
    fun deleteApplication_removesApplication() = runTest {
        repository.upsertApplication(sampleApplication)

        repository.deleteApplication(sampleApplication.id)

        assertThat(repository.observeApplications().first()).isEmpty()
    }

    @Test
    fun sendApplications_replacesAllApplications() = runTest {
        repository.upsertApplication(sampleApplication)

        repository.sendApplications(emptyList())

        assertThat(repository.observeApplications().first()).isEmpty()
    }
}
