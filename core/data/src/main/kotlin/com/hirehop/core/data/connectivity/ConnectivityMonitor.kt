package com.hirehop.core.data.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityMonitor {
    val isOnline: Flow<Boolean>
}

interface MockConnectivityControl {
    fun setOnline(online: Boolean)
}
