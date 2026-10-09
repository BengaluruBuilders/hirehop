package com.tailormyresume.core.data.connectivity

import android.net.ConnectivityManager
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkConnectivityMonitorRegistrationTest {
    private val network = FakeNetwork()

    @Test
    fun aSecurityExceptionOnRegistrationKeepsTheInitialOnlineValue() {
        network.connect()

        assertThat(monitorWhoseRegistrationThrows(SecurityException("uid")).isOnline.value).isTrue()
    }

    @Test
    fun aSecurityExceptionOnRegistrationKeepsTheInitialOfflineValue() {
        network.connectWithoutInternetCapability()

        assertThat(monitorWhoseRegistrationThrows(SecurityException("uid")).isOnline.value).isFalse()
    }

    @Test
    fun anyOtherExceptionOnRegistrationWithNoInitialValueFallsBackToOnline() {
        network.noActiveNetwork()

        assertThat(monitorWhoseRegistrationThrows(IllegalStateException("too many")).isOnline.value).isTrue()
    }

    private fun monitorWhoseRegistrationThrows(failure: Exception) =
        NetworkConnectivityMonitor(network.context) { _: ConnectivityManager, _ -> throw failure }
}
