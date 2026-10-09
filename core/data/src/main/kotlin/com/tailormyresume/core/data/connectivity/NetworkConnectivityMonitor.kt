package com.tailormyresume.core.data.connectivity

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SuppressLint("MissingPermission")
class NetworkConnectivityMonitor internal constructor(
    context: Context,
    private val register: (ConnectivityManager, ConnectivityManager.NetworkCallback) -> Unit,
) : ConnectivityMonitor {

    constructor(context: Context) : this(
        context,
        { manager, callback -> manager.registerDefaultNetworkCallback(callback) },
    )

    private val manager = context.getSystemService(ConnectivityManager::class.java)
    private val lock = Any()
    private val initialCapabilities = manager.getNetworkCapabilities(manager.activeNetwork)
    private var networkUp = hasInternet(initialCapabilities)
    private var overriddenOffline = false
    private val online = MutableStateFlow(networkUp)

    override val isOnline: StateFlow<Boolean> = online.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            updateNetwork(hasInternet(capabilities))
        }

        override fun onLost(network: Network) {
            updateNetwork(false)
        }
    }

    init {
        val registered = runCatching { register(manager, callback) }.isSuccess
        if (!registered && initialCapabilities == null) updateNetwork(true)
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
