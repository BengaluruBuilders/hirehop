package com.tailormyresume.core.data.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineConnectivityMonitor @Inject constructor() : ConnectivityMonitor, MockConnectivityControl {

    private val online = MutableStateFlow(true)

    override val isOnline: Flow<Boolean> = online

    override fun setOnline(online: Boolean) {
        this.online.value = online
    }
}
