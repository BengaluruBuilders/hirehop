package com.hirehop.core.data.connectivity

import com.hirehop.core.testing.connectivity.ConnectivityMonitorContractTest

class OfflineConnectivityMonitorTest : ConnectivityMonitorContractTest() {

    override fun createFixture(): Fixture {
        val monitor = OfflineConnectivityMonitor()
        return Fixture(monitor, monitor)
    }
}
