package com.hirehop.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.model.testApplication
import com.hirehop.core.data.model.testBareApplication
import com.hirehop.core.model.ApplicationStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstApplicationRepositoryTest {

    private val now = Instant.fromEpochMilliseconds(1_800_000_000_000)
    private val fixedClock = object : Clock {
        override fun now(): Instant = now
    }
    private val repository = OfflineFirstApplicationRepository(
        jobApplicationDao = FakeJobApplicationDao(),
        clock = fixedClock,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun upsertedApplicationIsObserved() = runTest {
        repository.upsertApplication(testApplication)

        assertThat(repository.observeApplication(testApplication.id).first())
            .isEqualTo(testApplication)
    }

    @Test
    fun unknownApplicationIsObservedAsNull() = runTest {
        assertThat(repository.observeApplication("missing").first()).isNull()
    }

    @Test
    fun applicationsAreOrderedByMostRecentUpdate() = runTest {
        val older = testBareApplication.copy(updatedAt = Instant.fromEpochMilliseconds(1))
        repository.upsertApplication(older)
        repository.upsertApplication(testApplication)

        assertThat(repository.observeApplications().first())
            .containsExactly(testApplication, older).inOrder()
    }

    @Test
    fun updateStatusChangesStatusAndStampsUpdatedAt() = runTest {
        repository.upsertApplication(testApplication)

        repository.updateStatus(testApplication.id, ApplicationStatus.OFFER)

        val saved = repository.observeApplication(testApplication.id).first()
        assertThat(saved?.status).isEqualTo(ApplicationStatus.OFFER)
        assertThat(saved?.updatedAt).isEqualTo(now)
        assertThat(saved?.createdAt).isEqualTo(testApplication.createdAt)
    }

    @Test
    fun updateNotesChangesNotesAndStampsUpdatedAt() = runTest {
        repository.upsertApplication(testApplication)

        repository.updateNotes(testApplication.id, "Follow up on Monday")

        val saved = repository.observeApplication(testApplication.id).first()
        assertThat(saved?.notes).isEqualTo("Follow up on Monday")
        assertThat(saved?.updatedAt).isEqualTo(now)
    }

    @Test
    fun deletedApplicationIsGone() = runTest {
        repository.upsertApplication(testApplication)

        repository.deleteApplication(testApplication.id)

        assertThat(repository.observeApplications().first()).isEmpty()
    }
}
