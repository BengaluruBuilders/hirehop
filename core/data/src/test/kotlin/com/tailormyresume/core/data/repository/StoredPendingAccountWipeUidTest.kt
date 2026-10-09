package com.tailormyresume.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.testing.mock.TestMockStateStore
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StoredPendingAccountWipeUidTest {

    private val store = TestMockStateStore()

    @Test
    fun theDeletingAccountSurvivesRestartAndTheServerClosedStep() = runTest {
        StoredPendingAccountWipe(store).apply {
            markRequested()
            recordUid("uid-a")
        }
        StoredPendingAccountWipe(store).markServerClosed()

        assertThat(StoredPendingAccountWipe(store).uid()).isEqualTo("uid-a")
    }

    @Test
    fun clearForgetsTheAccount() = runTest {
        val wipe = StoredPendingAccountWipe(store)
        wipe.markRequested()
        wipe.recordUid("uid-a")

        wipe.clear()

        assertThat(wipe.uid()).isNull()
    }

    @Test
    fun aNewRequestDoesNotInheritAnEarlierAccount() = runTest {
        val wipe = StoredPendingAccountWipe(store)
        wipe.markRequested()
        wipe.recordUid("uid-a")

        wipe.markRequested()

        assertThat(wipe.uid()).isNull()
    }
}
