package com.tailormyresume.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.mock.MockStateStore
import com.tailormyresume.core.testing.mock.TestMockStateStore
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StoredPendingAccountWipeAtomicTest {

    private val inner = TestMockStateStore()
    private val writes = mutableListOf<String>()
    private val store = object : MockStateStore by inner {
        override suspend fun write(key: String, value: String) {
            writes += key
            inner.write(key, value)
        }
    }

    @Test
    fun stateAndUidAreOneWrite() = runTest {
        val wipe = StoredPendingAccountWipe(store)

        wipe.markRequested("uid-a")

        assertThat(writes).hasSize(1)
        assertThat(StoredPendingAccountWipe(inner).state()).isEqualTo(PendingWipeState.REQUESTED)
        assertThat(StoredPendingAccountWipe(inner).uid()).isEqualTo("uid-a")
    }

    @Test
    fun markServerClosedKeepsTheEncodedUid() = runTest {
        val wipe = StoredPendingAccountWipe(inner)
        wipe.markRequested("uid-a")

        wipe.markServerClosed()

        assertThat(wipe.state()).isEqualTo(PendingWipeState.SERVER_CLOSED)
        assertThat(wipe.uid()).isEqualTo("uid-a")
    }

    @Test
    fun clearRemovesTheEncodedUid() = runTest {
        val wipe = StoredPendingAccountWipe(inner)
        wipe.markRequested("uid-a")

        wipe.clear()

        assertThat(wipe.state()).isEqualTo(PendingWipeState.NONE)
        assertThat(wipe.uid()).isNull()
    }

    @Test
    fun legacySeparateUidKeyStillReads() = runTest {
        inner.write("account.pendingWipe", "REQUESTED")
        inner.write("account.pendingWipe.uid", "uid-a")

        val wipe = StoredPendingAccountWipe(inner)

        assertThat(wipe.state()).isEqualTo(PendingWipeState.REQUESTED)
        assertThat(wipe.uid()).isEqualTo("uid-a")
    }

    @Test
    fun aNewRequestWithoutAUidDoesNotInheritALegacyUid() = runTest {
        inner.write("account.pendingWipe.uid", "uid-old")
        val wipe = StoredPendingAccountWipe(inner)

        wipe.markRequested()

        assertThat(wipe.uid()).isNull()
    }
}
