package com.tailormyresume.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.testing.mock.TestMockStateStore
import com.tailormyresume.core.testing.repository.TailoringReviewStateRepositoryContractTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StoredTailoringReviewStateRepositoryTest : TailoringReviewStateRepositoryContractTest() {

    override fun createTailoringReviewStateRepository(): TailoringReviewStateRepository =
        StoredTailoringReviewStateRepository(TestMockStateStore())

    @Test
    fun aLegacyRegenerationCountIsStillRead() = runTest {
        val store = TestMockStateStore()
        store.write("tailor.regenerations.app-1", "2")
        store.write("tailor.edited.app-1", "b1|b2")

        val state = StoredTailoringReviewStateRepository(store).observe("app-1").first()

        assertThat(state.regenerationsUsed).isEqualTo(2)
        assertThat(state.editedBulletIds).containsExactly("b1", "b2")
    }

    @Test
    fun aNewRegenerationKeepsTheLegacyCount() = runTest {
        val store = TestMockStateStore()
        store.write("tailor.regenerations.app-1", "2")
        val repository = StoredTailoringReviewStateRepository(store)

        repository.recordRegeneration("app-1", "PROJECT")

        assertThat(repository.observe("app-1").first().regenerationsUsed).isEqualTo(3)
    }

    @Test
    fun theStateSurvivesARestartOfTheRepository() = runTest {
        val store = TestMockStateStore()
        StoredTailoringReviewStateRepository(store).apply {
            recordRegeneration("app-1", "EXPERIENCE")
            markEdited("app-1", "b1")
        }

        val state = StoredTailoringReviewStateRepository(store).observe("app-1").first()

        assertThat(state.regenerationsUsedIn("EXPERIENCE")).isEqualTo(1)
        assertThat(state.editedBulletIds).containsExactly("b1")
    }
}
