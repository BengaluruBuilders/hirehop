package com.tailormyresume.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.model.testApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

class OfflineFirstApplicationRepositoryArtefactTest {

    private val cleared = mutableListOf<String>()
    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1_800_000_000_000)
    }

    private fun TestScope.newRepository() = OfflineFirstApplicationRepository(
        jobApplicationDao = FakeJobApplicationDao(),
        clock = clock,
        cleanup = ApplicationCleanup { cleared += it },
        ioDispatcher = UnconfinedTestDispatcher(testScheduler),
    )

    @Test
    fun deleteApplicationRowKeepsArtefacts() = runTest {
        val repository = newRepository()
        repository.upsertApplication(testApplication)

        repository.deleteApplicationRow(testApplication.id)

        assertThat(repository.observeApplication(testApplication.id).first()).isNull()
        assertThat(cleared).isEmpty()
    }

    @Test
    fun clearArtefactsKeepsTheRow() = runTest {
        val repository = newRepository()
        repository.upsertApplication(testApplication)

        repository.clearArtefacts(testApplication.id)

        assertThat(cleared).containsExactly(testApplication.id)
        assertThat(repository.observeApplication(testApplication.id).first()).isEqualTo(testApplication)
    }

    @Test
    fun deleteApplicationStillClearsArtefactsAndRow() = runTest {
        val repository = newRepository()
        repository.upsertApplication(testApplication)

        repository.deleteApplication(testApplication.id)

        assertThat(cleared).containsExactly(testApplication.id)
        assertThat(repository.observeApplication(testApplication.id).first()).isNull()
    }
}
