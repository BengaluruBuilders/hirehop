package com.hirehop.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.testing.mock.TestMockStateStore
import com.hirehop.core.testing.repository.UsageAllowanceContractTest
import com.hirehop.core.testing.util.TestClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StoredUsageAllowanceTest : UsageAllowanceContractTest() {

    override fun createUsageAllowance(clock: TestClock): UsageAllowance =
        StoredUsageAllowance(TestMockStateStore(), clock)

    @Test
    fun usageSurvivesARestartOfTheAllowance() = runTest {
        val store = TestMockStateStore()
        val clock = TestClock()
        StoredUsageAllowance(store, clock).consumeAnalysis()

        assertThat(StoredUsageAllowance(store, clock).observeAnalysesLeft().first()).isEqualTo(2)
    }
}
