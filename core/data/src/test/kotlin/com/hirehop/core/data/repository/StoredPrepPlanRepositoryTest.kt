package com.hirehop.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.PrepPlanItem
import com.hirehop.core.testing.mock.TestMockStateStore
import com.hirehop.core.testing.repository.PrepPlanRepositoryContractTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StoredPrepPlanRepositoryTest : PrepPlanRepositoryContractTest() {

    override fun createPrepPlanRepository(): PrepPlanRepository = StoredPrepPlanRepository(TestMockStateStore())

    @Test
    fun thePlanSurvivesARestartOfTheRepository() = runTest {
        val store = TestMockStateStore()
        val item = PrepPlanItem("q1", "Explain Room", done = true)
        StoredPrepPlanRepository(store).add("app-1", item)

        assertThat(StoredPrepPlanRepository(store).observeItems("app-1").first()).containsExactly(item)
    }
}
