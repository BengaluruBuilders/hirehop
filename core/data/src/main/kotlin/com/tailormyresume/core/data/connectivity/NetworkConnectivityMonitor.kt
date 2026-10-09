package com.tailormyresume.core.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkConnectivityMonitor @Inject constructor(
    @ApplicationContext context: Context,
) : ConnectivityMonitor {

    private val manager = context.getSystemService(ConnectivityManager::class.java)
    private val lock = Any()
    private var networkUp = hasInternet(manager.getNetworkCapabilities(manager.activeNetwork))
    private var overriddenOffline = false
    private val online = MutableStateFlow(networkUp)

    override val isOnline: StateFlow<Boolean> = online.asStateFlow()

    init {
        manager.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                    updateNetwork(hasInternet(capabilities))
                }

                override fun onLost(network: Network) {
                    updateNetwork(false)
                }
            },
        )
    }

    fun overrideOffline(overridden: Boolean) = synchronized(lock) {
        overriddenOffline = overridden
        online.value = networkUp && !overriddenOffline
    }

    private fun updateNetwork(up: Boolean) = synchronized(lock) {
        networkUp = up
        online.value = networkUp && !overriddenOffline
    }

    private fun hasInternet(capabilities: NetworkCapabilities?): Boolean =
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}
