package com.tailormyresume.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.testing.mock.TestMockStateStore
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StoredPendingAccountWipeTest {

    private val store = TestMockStateStore()

    @Test
    fun startsWithNothingPending() = runTest {
        assertThat(StoredPendingAccountWipe(store).state()).isEqualTo(PendingWipeState.NONE)
    }

    @Test
    fun survivesRestart() = runTest {
        StoredPendingAccountWipe(store).markRequested()
        assertThat(StoredPendingAccountWipe(store).state()).isEqualTo(PendingWipeState.REQUESTED)

        StoredPendingAccountWipe(store).markServerClosed()
        assertThat(StoredPendingAccountWipe(store).state()).isEqualTo(PendingWipeState.SERVER_CLOSED)
    }

    @Test
    fun clearReturnsToNothingPending() = runTest {
        val wipe = StoredPendingAccountWipe(store)
        wipe.markServerClosed()

        wipe.clear()

        assertThat(wipe.state()).isEqualTo(PendingWipeState.NONE)
    }

    @Test
    fun clearingTheWholeStoreCompletesThePendingWipe() = runTest {
        val wipe = StoredPendingAccountWipe(store)
        wipe.markServerClosed()

        store.clear()

        assertThat(wipe.state()).isEqualTo(PendingWipeState.NONE)
    }

    @Test
    fun anUnreadableValueMeansNothingPending() = runTest {
        store.write("account.pendingWipe", "garbage")

        assertThat(StoredPendingAccountWipe(store).state()).isEqualTo(PendingWipeState.NONE)
    }
}
