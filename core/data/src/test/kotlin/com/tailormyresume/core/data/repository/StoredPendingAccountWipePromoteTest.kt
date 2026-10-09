package com.tailormyresume.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.testing.mock.TestMockStateStore
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StoredPendingAccountWipePromoteTest {

    private val store = TestMockStateStore()
    private val wipe = StoredPendingAccountWipe(store)

    @Test
    fun requestedMarkerOfTheSameUidIsPromotedAndKeepsItsUid() = runTest {
        wipe.markRequested("uid-a")

        val promoted = wipe.promoteToServerClosed("uid-a")

        assertThat(promoted).isTrue()
        assertThat(wipe.state()).isEqualTo(PendingWipeState.SERVER_CLOSED)
        assertThat(wipe.uid()).isEqualTo("uid-a")
    }

    @Test
    fun markerOfAnotherUidIsNotPromoted() = runTest {
        wipe.markRequested("uid-a")

        assertThat(wipe.promoteToServerClosed("uid-b")).isFalse()
        assertThat(wipe.state()).isEqualTo(PendingWipeState.REQUESTED)
    }

    @Test
    fun uidlessMarkerIsNotPromoted() = runTest {
        wipe.markRequested()

        assertThat(wipe.promoteToServerClosed("uid-a")).isFalse()
        assertThat(wipe.state()).isEqualTo(PendingWipeState.REQUESTED)
    }

    @Test
    fun clearedMarkerIsNotRevived() = runTest {
        wipe.markRequested("uid-a")
        wipe.clear()

        assertThat(wipe.promoteToServerClosed("uid-a")).isFalse()
        assertThat(wipe.state()).isEqualTo(PendingWipeState.NONE)
    }

    @Test
    fun serverClosedMarkerIsLeftAlone() = runTest {
        wipe.markRequested("uid-a")
        wipe.markServerClosed()

        assertThat(wipe.promoteToServerClosed("uid-a")).isFalse()
        assertThat(wipe.state()).isEqualTo(PendingWipeState.SERVER_CLOSED)
    }

    @Test
    fun legacyMarkerWithSeparateUidKeyIsPromoted() = runTest {
        store.write("account.pendingWipe", "REQUESTED")
        store.write("account.pendingWipe.uid", "uid-a")

        assertThat(wipe.promoteToServerClosed("uid-a")).isTrue()
        assertThat(wipe.state()).isEqualTo(PendingWipeState.SERVER_CLOSED)
    }
}
