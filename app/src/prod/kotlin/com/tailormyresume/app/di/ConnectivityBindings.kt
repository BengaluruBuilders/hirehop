package com.tailormyresume.app.di

import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.connectivity.DebugOverridableConnectivityMonitor
import com.tailormyresume.core.data.connectivity.MockConnectivityControl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ConnectivityBindings {
    @Binds
    fun bindConnectivityMonitor(impl: DebugOverridableConnectivityMonitor): ConnectivityMonitor

    @Binds
    fun bindMockConnectivityControl(impl: DebugOverridableConnectivityMonitor): MockConnectivityControl
}
