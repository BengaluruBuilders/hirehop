package com.tailormyresume.app.di

import android.content.Context
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.connectivity.DebugOverridableConnectivityMonitor
import com.tailormyresume.core.data.connectivity.MockConnectivityControl
import com.tailormyresume.core.data.connectivity.NetworkConnectivityMonitor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectivityBindings {
    @Binds
    abstract fun bindConnectivityMonitor(impl: DebugOverridableConnectivityMonitor): ConnectivityMonitor

    @Binds
    abstract fun bindMockConnectivityControl(impl: DebugOverridableConnectivityMonitor): MockConnectivityControl

    companion object {
        @Provides
        @Singleton
        fun providesNetworkConnectivityMonitor(@ApplicationContext context: Context): NetworkConnectivityMonitor =
            NetworkConnectivityMonitor(context)

        @Provides
        @Singleton
        fun providesDebugOverridableConnectivityMonitor(
            network: NetworkConnectivityMonitor,
        ): DebugOverridableConnectivityMonitor = DebugOverridableConnectivityMonitor(network)
    }
}
