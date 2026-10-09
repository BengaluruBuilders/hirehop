package com.tailormyresume.core.data.connectivity

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.testing.connectivity.ConnectivityMonitorContractTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DebugOverridableConnectivityMonitorTest : ConnectivityMonitorContractTest() {
    private val network = FakeNetwork()

    override fun createFixture(): Fixture {
        network.connect()
        val monitor = DebugOverridableConnectivityMonitor(NetworkConnectivityMonitor(network.context))
        return Fixture(monitor, monitor)
    }

    @Test
    fun reportsOfflineWhenTheNetworkIsDownEvenIfTheToggleIsOnline() {
        val fixture = createFixture()

        network.disconnect()

        assertThat(fixture.monitor.current()).isFalse()
    }

    @Test
    fun staysOfflineAfterTheNetworkReturnsWhileTheToggleIsOffline() {
        val fixture = createFixture()
        fixture.control.setOnline(false)

        network.disconnect()
        network.reconnect()

        assertThat(fixture.monitor.current()).isFalse()
    }

    @Test
    fun comesBackOnlineWhenTheToggleIsClearedAndTheNetworkIsUp() {
        val fixture = createFixture()
        fixture.control.setOnline(false)

        fixture.control.setOnline(true)

        assertThat(fixture.monitor.current()).isTrue()
    }

    private fun ConnectivityMonitor.current(): Boolean = runBlocking { isOnline.first() }
}
