package com.hirehop.core.testing.repository

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.JobApplication
import com.hirehop.core.testing.data.canonicalApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.hours

abstract class ApplicationRepositoryContractTest {

    protected abstract fun createApplicationRepository(): ApplicationRepository

    @Test
    fun aNewAccountHasNoApplications() = runTest {
        val repository = createApplicationRepository()

        assertThat(repository.observeApplications().first()).isEmpty()
    }

    @Test
    fun aSavedApplicationComesBackUnchanged() = runTest {
        val repository = createApplicationRepository()
        val application: JobApplication = canonicalApplication

        repository.upsertApplication(application)

        assertThat(repository.observeApplication(application.id).first()).isEqualTo(application)
        assertThat(repository.observeApplications().first()).containsExactly(application)
    }

    @Test
    fun anUnknownApplicationIsNotFound() = runTest {
        val repository = createApplicationRepository()

        assertThat(repository.observeApplication(UNKNOWN_ID).first()).isNull()
    }

    @Test
    fun savingTheSameIdTwiceReplacesTheApplication() = runTest {
        val repository = createApplicationRepository()
        val application: JobApplication = canonicalApplication
        repository.upsertApplication(application)

        val renamed = application.copy(notes = "Applied through the college portal")
        repository.upsertApplication(renamed)

        assertThat(repository.observeApplications().first()).hasSize(1)
        assertThat(repository.observeApplication(application.id).first()).isEqualTo(renamed)
    }

    @Test
    fun aStatusChangeIsVisibleToTheCaller() = runTest {
        val repository = createApplicationRepository()
        val application: JobApplication = canonicalApplication
        repository.upsertApplication(application)

        repository.updateStatus(application.id, ApplicationStatus.APPLIED)

        assertThat(repository.observeApplication(application.id).first()?.status)
            .isEqualTo(ApplicationStatus.APPLIED)
    }

    @Test
    fun aStatusChangeForAnUnknownIdChangesNothing() = runTest {
        val repository = createApplicationRepository()
        repository.upsertApplication(canonicalApplication)

        repository.updateStatus(UNKNOWN_ID, ApplicationStatus.REJECTED)

        assertThat(repository.observeApplications().first().map { it.id })
            .containsExactly(canonicalApplication.id)
    }

    @Test
    fun aNotesChangeIsVisibleToTheCaller() = runTest {
        val repository = createApplicationRepository()
        val application: JobApplication = canonicalApplication
        repository.upsertApplication(application)

        repository.updateNotes(application.id, "Recruiter called on 12 Mar")

        assertThat(repository.observeApplication(application.id).first()?.notes)
            .isEqualTo("Recruiter called on 12 Mar")
    }

    @Test
    fun deletingRemovesTheApplication() = runTest {
        val repository = createApplicationRepository()
        repository.upsertApplication(canonicalApplication)

        repository.deleteApplication(canonicalApplication.id)

        assertThat(repository.observeApplications().first()).isEmpty()
        assertThat(repository.observeApplication(canonicalApplication.id).first()).isNull()
    }

    @Test
    fun deletingAnUnknownIdChangesNothing() = runTest {
        val repository = createApplicationRepository()
        repository.upsertApplication(canonicalApplication)

        repository.deleteApplication(UNKNOWN_ID)

        assertThat(repository.observeApplications().first()).hasSize(1)
    }

    @Test
    fun theMostRecentlyUpdatedApplicationComesFirst() = runTest {
        val repository = createApplicationRepository()
        val older: JobApplication = canonicalApplication
        val newer = canonicalApplication.copy(
            id = "application-northwind-2",
            updatedAt = older.updatedAt + 1.hours,
        )
        repository.upsertApplication(older)
        repository.upsertApplication(newer)

        val ids = repository.observeApplications().first().map { it.id }

        assertThat(ids).isEqualTo(listOf(newer.id, older.id))
    }

    private companion object {
        const val UNKNOWN_ID = "application_that_does_not_exist"
    }
}
