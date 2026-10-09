package com.tailormyresume.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.CareerStage
import com.tailormyresume.core.testing.mock.TestMockStateStore
import com.tailormyresume.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StoredSessionRepositoryCareerStageTest {

    @Test
    fun signOutClearsTheCareerStage() = runTest {
        val session = StoredSessionRepository(TestMockStateStore())
        session.saveCareerStage(CareerStage.ONE_TO_TWO_YEARS_IN)

        session.signOut()

        assertThat(session.observeCareerStage().first()).isNull()
    }

    @Test
    fun theTestFakeSignOutClearsTheCareerStageToo() = runTest {
        val session = TestSessionRepository()
        session.saveCareerStage(CareerStage.ONE_TO_TWO_YEARS_IN)

        session.signOut()

        assertThat(session.observeCareerStage().first()).isNull()
    }
}
