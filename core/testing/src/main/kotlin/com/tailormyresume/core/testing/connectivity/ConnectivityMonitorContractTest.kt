package com.tailormyresume.core.testing.connectivity

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.connectivity.MockConnectivityControl
import kotlinx.coroutines.test.runTest
import org.junit.Test

abstract class ConnectivityMonitorContractTest {

    protected class Fixture(val monitor: ConnectivityMonitor, val control: MockConnectivityControl)

    protected abstract fun createFixture(): Fixture

    @Test
    fun theMonitorStartsOnline() = runTest {
        val fixture = createFixture()

        fixture.monitor.isOnline.test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun theControlChangesWhatTheMonitorReports() = runTest {
        val fixture = createFixture()

        fixture.monitor.isOnline.test {
            assertThat(awaitItem()).isTrue()
            fixture.control.setOnline(false)
            assertThat(awaitItem()).isFalse()
            fixture.control.setOnline(true)
            assertThat(awaitItem()).isTrue()
        }
    }
}
