package com.tailormyresume.app.di

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.connectivity.MockConnectivityControl
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities

@RunWith(RobolectricTestRunner::class)
class ConnectivityBindingsTest {
    private val application = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun losingTheDefaultNetworkMakesTheBoundMonitorOfflineThroughTheBoundControl() {
        val capabilities = ShadowNetworkCapabilities.newInstance()
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val manager = application.getSystemService(ConnectivityManager::class.java)
        val shadow = shadowOf(manager)
        shadow.setNetworkCapabilities(manager.activeNetwork, capabilities)
        val network = ConnectivityBindings.providesNetworkConnectivityMonitor(application)
        val bound = ConnectivityBindings.providesDebugOverridableConnectivityMonitor(network)
        shadow.networkCallbacks.forEach { it.onCapabilitiesChanged(ShadowNetwork.newInstance(1), capabilities) }
        assertThat(bound.isOnline.value).isTrue()

        shadow.networkCallbacks.forEach { it.onLost(ShadowNetwork.newInstance(1)) }

        assertThat(bound.isOnline.value).isFalse()
        shadow.networkCallbacks.forEach { it.onCapabilitiesChanged(ShadowNetwork.newInstance(1), capabilities) }
        val control: MockConnectivityControl = bound
        control.setOnline(false)
        assertThat(bound.isOnline.value).isFalse()
        control.setOnline(true)
        assertThat(bound.isOnline.value).isTrue()
    }
}
