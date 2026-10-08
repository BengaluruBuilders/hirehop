package com.tailormyresume.core.data.connectivity

import com.tailormyresume.core.testing.connectivity.ConnectivityMonitorContractTest

class OfflineConnectivityMonitorTest : ConnectivityMonitorContractTest() {

    override fun createFixture(): Fixture {
        val monitor = OfflineConnectivityMonitor()
        return Fixture(monitor, monitor)
    }
}
