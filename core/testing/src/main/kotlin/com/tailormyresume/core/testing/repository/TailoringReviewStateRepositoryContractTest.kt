package com.tailormyresume.core.testing.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.TailoringReviewStateRepository
import com.tailormyresume.core.model.TailoringReviewState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

abstract class TailoringReviewStateRepositoryContractTest {

    protected abstract fun createTailoringReviewStateRepository(): TailoringReviewStateRepository

    @Test
    fun aNewApplicationHasAnEmptyState() = runTest {
        val state = createTailoringReviewStateRepository().observe("app-1").first()

        assertThat(state).isEqualTo(TailoringReviewState())
        assertThat(state.regenerationsUsed).isEqualTo(0)
    }

    @Test
    fun regenerationsAreCountedPerSectionAndInTotal() = runTest {
        val repository = createTailoringReviewStateRepository()

        repository.recordRegeneration("app-1", "EXPERIENCE")
        repository.recordRegeneration("app-1", "EXPERIENCE")
        repository.recordRegeneration("app-1", "PROJECT")

        val state = repository.observe("app-1").first()
        assertThat(state.regenerationsUsedIn("EXPERIENCE")).isEqualTo(2)
        assertThat(state.regenerationsUsedIn("PROJECT")).isEqualTo(1)
        assertThat(state.regenerationsUsedIn("EDUCATION")).isEqualTo(0)
        assertThat(state.regenerationsUsed).isEqualTo(3)
    }

    @Test
    fun editedBulletsCanBeMarkedAndCleared() = runTest {
        val repository = createTailoringReviewStateRepository()
        repository.markEdited("app-1", "b1")
        repository.markEdited("app-1", "b2")
        repository.markEdited("app-1", "b1")

        assertThat(repository.observe("app-1").first().editedBulletIds).containsExactly("b1", "b2")

        repository.clearEdited("app-1", listOf("b1"))

        assertThat(repository.observe("app-1").first().editedBulletIds).containsExactly("b2")
    }

    @Test
    fun eachApplicationHasItsOwnState() = runTest {
        val repository = createTailoringReviewStateRepository()
        repository.recordRegeneration("app-1", "PROJECT")
        repository.markEdited("app-1", "b1")

        assertThat(repository.observe("app-2").first()).isEqualTo(TailoringReviewState())
    }

    @Test
    fun clearForResetsOnlyThatApplication() = runTest {
        val repository = createTailoringReviewStateRepository()
        repository.recordRegeneration("app-1", "PROJECT")
        repository.markEdited("app-1", "b1")
        repository.markEdited("app-2", "b2")

        repository.clearFor("app-1")

        assertThat(repository.observe("app-1").first()).isEqualTo(TailoringReviewState())
        assertThat(repository.observe("app-2").first().editedBulletIds).containsExactly("b2")
    }
}
