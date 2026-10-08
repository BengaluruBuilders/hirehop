package com.tailormyresume.core.testing.connectivity

import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.connectivity.MockConnectivityControl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestConnectivityMonitor(initiallyOnline: Boolean = true) : ConnectivityMonitor, MockConnectivityControl {

    private val online = MutableStateFlow(initiallyOnline)

    override val isOnline: Flow<Boolean> = online

    override fun setOnline(online: Boolean) {
        this.online.value = online
    }
}
