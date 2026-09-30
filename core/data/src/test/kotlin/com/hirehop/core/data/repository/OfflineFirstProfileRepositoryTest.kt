package com.hirehop.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.model.testEntries
import com.hirehop.core.data.model.testProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstProfileRepositoryTest {

    private fun TestScope.newRepository() = OfflineFirstProfileRepository(
        profileDao = FakeProfileDao(),
        ioDispatcher = UnconfinedTestDispatcher(testScheduler),
    )

    @Test
    fun emptyStoreObservesNull() = runTest {
        val repository = newRepository()
        assertThat(repository.observeProfile().first()).isNull()
    }

    @Test
    fun savedProfileIsObserved() = runTest {
        val repository = newRepository()
        repository.saveProfile(testProfile)

        assertThat(repository.observeProfile().first()).isEqualTo(testProfile)
    }

    @Test
    fun savingAgainReplacesEntries() = runTest {
        val repository = newRepository()
        repository.saveProfile(testProfile)
        val reduced = testProfile.copy(entries = testEntries.take(1))

        repository.saveProfile(reduced)

        assertThat(repository.observeProfile().first()).isEqualTo(reduced)
    }

    @Test
    fun clearRemovesProfile() = runTest {
        val repository = newRepository()
        repository.saveProfile(testProfile)

        repository.clearProfile()

        assertThat(repository.observeProfile().first()).isNull()
    }
}
