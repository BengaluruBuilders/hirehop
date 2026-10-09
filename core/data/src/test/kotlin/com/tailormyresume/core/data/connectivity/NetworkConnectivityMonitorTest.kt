package com.tailormyresume.core.data.connectivity

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkConnectivityMonitorTest {
    private val network = FakeNetwork()

    @Test
    fun startsOnlineWhenTheActiveNetworkHasInternet() {
        network.connect()

        assertThat(NetworkConnectivityMonitor(network.context).isOnline.value).isTrue()
    }

    @Test
    fun startsOfflineWhenNoNetworkIsActive() {
        assertThat(NetworkConnectivityMonitor(network.context).isOnline.value).isFalse()
    }

    @Test
    fun losingTheNetworkEmitsOffline() {
        network.connect()
        val monitor = NetworkConnectivityMonitor(network.context)

        network.disconnect()

        assertThat(monitor.isOnline.value).isFalse()
    }

    @Test
    fun regainingTheNetworkEmitsOnline() {
        network.connect()
        val monitor = NetworkConnectivityMonitor(network.context)
        network.disconnect()

        network.reconnect()

        assertThat(monitor.isOnline.value).isTrue()
    }
}
