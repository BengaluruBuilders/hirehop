package com.tailormyresume.core.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowConnectivityManager
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowNetworkInfo

internal class FakeNetwork {
    val context: Context = ApplicationProvider.getApplicationContext()
    private val manager = context.getSystemService(ConnectivityManager::class.java)
    private val shadow: ShadowConnectivityManager = shadowOf(manager)
    private val network: Network = ShadowNetwork.newInstance(NETWORK_ID)

    fun connect() {
        shadow.setActiveNetworkInfo(connectedWifi())
        shadow.setNetworkCapabilities(manager.activeNetwork, capabilitiesWithInternet())
    }

    fun disconnect() {
        shadow.networkCallbacks.forEach { callback -> callback.onLost(network) }
    }

    fun reconnect() {
        shadow.networkCallbacks.forEach { callback ->
            callback.onCapabilitiesChanged(network, capabilitiesWithInternet())
        }
    }

    private fun connectedWifi(): NetworkInfo = ShadowNetworkInfo.newInstance(
        NetworkInfo.DetailedState.CONNECTED,
        ConnectivityManager.TYPE_WIFI,
        0,
        true,
        NetworkInfo.State.CONNECTED,
    )

    private fun capabilitiesWithInternet(): NetworkCapabilities {
        val capabilities = ShadowNetworkCapabilities.newInstance()
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        return capabilities
    }

    private companion object {
        const val NETWORK_ID = 42
    }
}
