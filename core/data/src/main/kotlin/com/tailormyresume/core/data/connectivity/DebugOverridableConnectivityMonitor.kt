package com.tailormyresume.core.data.connectivity

import kotlinx.coroutines.flow.StateFlow

class DebugOverridableConnectivityMonitor(private val network: NetworkConnectivityMonitor) : ConnectivityMonitor, MockConnectivityControl {

    override val isOnline: StateFlow<Boolean> = network.isOnline

    override fun setOnline(online: Boolean) = network.overrideOffline(!online)
}
