package com.tailormyresume.core.data.connectivity

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkConnectivityMonitor(@Suppress("UNUSED_PARAMETER") context: Context) : ConnectivityMonitor {
    override val isOnline: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
}
