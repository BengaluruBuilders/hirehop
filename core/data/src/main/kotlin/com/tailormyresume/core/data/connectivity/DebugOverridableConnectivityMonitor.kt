package com.tailormyresume.core.data.connectivity

import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugOverridableConnectivityMonitor @Inject constructor(
    private val network: NetworkConnectivityMonitor,
) : ConnectivityMonitor, MockConnectivityControl {

    override val isOnline: StateFlow<Boolean> = network.isOnline

    override fun setOnline(online: Boolean) = network.overrideOffline(!online)
}
