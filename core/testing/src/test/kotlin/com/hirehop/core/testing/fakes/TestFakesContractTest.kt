package com.hirehop.core.testing.fakes

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.mock.MockStateStore
import com.hirehop.core.data.repository.ContentReportRepository
import com.hirehop.core.data.repository.CoverLetterRepository
import com.hirehop.core.data.repository.ExportHistoryRepository
import com.hirehop.core.data.repository.PrepPlanRepository
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.data.repository.TailoringReviewStateRepository
import com.hirehop.core.data.repository.UsageAllowance
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.account.AccountDataExporter
import com.hirehop.core.testing.account.AccountDataExporterContractTest
import com.hirehop.core.testing.account.TestAccountDataExporter
import com.hirehop.core.testing.connectivity.ConnectivityMonitorContractTest
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.gateway.PaymentGatewayContractTest
import com.hirehop.core.testing.gateway.SignInGatewayContractTest
import com.hirehop.core.testing.gateway.TestPaymentGateway
import com.hirehop.core.testing.gateway.TestSignInGateway
import com.hirehop.core.testing.mock.MockStateStoreContractTest
import com.hirehop.core.testing.mock.TestMockStateStore
import com.hirehop.core.testing.repository.ContentReportRepositoryContractTest
import com.hirehop.core.testing.repository.CoverLetterRepositoryContractTest
import com.hirehop.core.testing.repository.ExportHistoryRepositoryContractTest
import com.hirehop.core.testing.repository.PrepPlanRepositoryContractTest
import com.hirehop.core.testing.repository.SessionRepositoryContractTest
import com.hirehop.core.testing.repository.TailoringReviewStateRepositoryContractTest
import com.hirehop.core.testing.repository.TestContentReportRepository
import com.hirehop.core.testing.repository.TestCoverLetterRepository
import com.hirehop.core.testing.repository.TestExportHistoryRepository
import com.hirehop.core.testing.repository.TestPrepPlanRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.repository.TestTailoringReviewStateRepository
import com.hirehop.core.testing.repository.TestUsageAllowance
import com.hirehop.core.testing.repository.UsageAllowanceContractTest
import com.hirehop.core.testing.sample.TestSampleDataController
import com.hirehop.core.testing.util.TestClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TestMockStateStoreContractTest : MockStateStoreContractTest() {
    override fun createStore(scope: CoroutineScope): MockStateStore = TestMockStateStore()
}

class TestSessionRepositoryContractTest : SessionRepositoryContractTest() {
    override fun createSessionRepository(): SessionRepository = TestSessionRepository()
}

class TestExportHistoryRepositoryContractTest : ExportHistoryRepositoryContractTest() {
    override fun createExportHistoryRepository(): ExportHistoryRepository = TestExportHistoryRepository()
}

class TestCoverLetterRepositoryContractTest : CoverLetterRepositoryContractTest() {
    override fun createCoverLetterRepository(): CoverLetterRepository = TestCoverLetterRepository()
}

class TestPrepPlanRepositoryContractTest : PrepPlanRepositoryContractTest() {
    override fun createPrepPlanRepository(): PrepPlanRepository = TestPrepPlanRepository()
}

class TestContentReportRepositoryContractTest : ContentReportRepositoryContractTest() {
    override fun createContentReportRepository(): ContentReportRepository = TestContentReportRepository()
}

class TestTailoringReviewStateRepositoryContractTest : TailoringReviewStateRepositoryContractTest() {
    override fun createTailoringReviewStateRepository(): TailoringReviewStateRepository =
        TestTailoringReviewStateRepository()
}

class TestUsageAllowanceContractTest : UsageAllowanceContractTest() {
    override fun createUsageAllowance(clock: TestClock): UsageAllowance = TestUsageAllowance(clock)
}

class TestConnectivityMonitorContractTest : ConnectivityMonitorContractTest() {
    override fun createFixture(): Fixture {
        val monitor = TestConnectivityMonitor()
        return Fixture(monitor, monitor)
    }
}

class TestAccountDataExporterContractTest : AccountDataExporterContractTest() {
    override fun createExporter(): AccountDataExporter = TestAccountDataExporter()
}

class TestPaymentGatewayContractTest : PaymentGatewayContractTest() {
    override fun createPaymentGateway(): PaymentGateway = TestPaymentGateway()
}

class TestSignInGatewayContractTest : SignInGatewayContractTest() {
    override fun createSignInGateway(): SignInGateway = TestSignInGateway()
}

class TestSampleDataControllerTest {
    @Test
    fun itCountsLoadsAndResets() = runTest {
        val controller = TestSampleDataController()

        controller.load()
        controller.reset()
        controller.reset()

        assertThat(controller.loadCount).isEqualTo(1)
        assertThat(controller.resetCount).isEqualTo(2)
    }
}
