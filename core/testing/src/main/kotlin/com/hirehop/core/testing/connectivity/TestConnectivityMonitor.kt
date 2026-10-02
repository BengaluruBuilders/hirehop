package com.hirehop.core.testing.connectivity

import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.data.connectivity.MockConnectivityControl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestConnectivityMonitor(initiallyOnline: Boolean = true) : ConnectivityMonitor, MockConnectivityControl {

    private val online = MutableStateFlow(initiallyOnline)

    override val isOnline: Flow<Boolean> = online

    override fun setOnline(online: Boolean) {
        this.online.value = online
    }
}
