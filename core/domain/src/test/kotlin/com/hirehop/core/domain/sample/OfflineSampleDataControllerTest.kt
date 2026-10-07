package com.hirehop.core.domain.sample

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.mock.NoMockLatency
import com.hirehop.core.domain.AnalyzeJobUseCase
import com.hirehop.core.domain.TailorResumeUseCase
import com.hirehop.core.domain.offline.OfflineFabricationGuard
import com.hirehop.core.domain.offline.OfflineGapMatcher
import com.hirehop.core.domain.offline.OfflineJobAnalysisSource
import com.hirehop.core.domain.offline.OfflineJobDescriptionAnalyzer
import com.hirehop.core.domain.offline.OfflinePaymentGateway
import com.hirehop.core.domain.offline.OfflineResumeTailor
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.CreditKind
import com.hirehop.core.model.JobApplication
import com.hirehop.core.testing.mock.TestMockStateStore
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestExportHistoryRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.sample.SampleDataControllerContractTest
import com.hirehop.core.testing.util.TestClock
import com.hirehop.core.testing.util.TestIdGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OfflineSampleDataControllerTest : SampleDataControllerContractTest() {

    private val store = TestMockStateStore()
    private val session = TestSessionRepository()
    private val profiles = TestProfileRepository()
    private val applications = TestApplicationRepository()
    private val exports = TestExportHistoryRepository()
    private val clock = TestClock()
    private val payments = OfflinePaymentGateway(store, NoMockLatency, clock, TestIdGenerator("order"))
    private val controller = OfflineSampleDataController(
        store = store,
        sessionRepository = session,
        profileRepository = profiles,
        applicationRepository = applications,
        exportHistoryRepository = exports,
        paymentGateway = payments,
        analyzeJob = AnalyzeJobUseCase(OfflineJobAnalysisSource(OfflineJobDescriptionAnalyzer(), OfflineGapMatcher())),
        tailorResume = TailorResumeUseCase(OfflineResumeTailor(), OfflineFabricationGuard()),
        clock = clock,
    )

    override fun createFixture() = Fixture(controller, session, profiles, applications, payments)

    private suspend fun loaded(): Map<String, JobApplication> {
        controller.load()
        return applications.observeApplications().first().associateBy { it.id }
    }

    @Test
    fun theFourApplicationsHaveTheAgreedTitlesCompaniesAndStatuses() = runTest {
        val loaded = loaded().values.map { Triple(it.job.title, it.job.company, it.status) }

        assertThat(loaded).containsExactly(
            Triple("Associate Analyst", "Northwind GCC", ApplicationStatus.APPLIED),
            Triple("Data Analyst Intern", "Paisa Ledger", ApplicationStatus.INTERVIEW),
            Triple("Graduate Engineer Trainee", "Sahyadri Motors", ApplicationStatus.SAVED),
            Triple("Business Analyst", "Meridian GCC", ApplicationStatus.NO_RESPONSE),
        )
    }

    @Test
    fun everyApplicationCarriesRealGapAnalysisAndTailoredBullets() = runTest {
        loaded().values.forEach { application ->
            assertThat(application.job.requirements).isNotEmpty()
            assertThat(application.gapAnalysis?.matches).isNotEmpty()
            assertThat(application.tailoredResume?.bullets).isNotEmpty()
            assertThat(checkNotNull(application.tailoredResume).bullets.all { it.sourceIds.isNotEmpty() }).isTrue()
        }
    }

    @Test
    fun theNorthwindApplicationIsFullyReviewedAndTheParsiLedgerOneIsPartlyReviewed() = runTest {
        val loaded = loaded()

        val northwind = checkNotNull(loaded.getValue("sample-northwind-associate-analyst").tailoredResume).bullets
        val paisa = checkNotNull(loaded.getValue("sample-paisa-ledger-data-analyst-intern").tailoredResume).bullets
        val sahyadri = checkNotNull(loaded.getValue("sample-sahyadri-motors-graduate-engineer-trainee").tailoredResume).bullets

        assertThat(northwind.none { it.decision == BulletDecision.PENDING }).isTrue()
        assertThat(paisa.any { it.decision == BulletDecision.PENDING }).isTrue()
        assertThat(paisa.any { it.decision != BulletDecision.PENDING }).isTrue()
        assertThat(sahyadri.all { it.decision == BulletDecision.PENDING }).isTrue()
    }

    @Test
    fun theNorthwindApplicationHasTwoExportsAndOnlyTheFirstSpendsACredit() = runTest {
        controller.load()

        val records = exports.observeExports("sample-northwind-associate-analyst").first()

        assertThat(records.map { it.creditKind }).containsExactly(CreditKind.FREE, null).inOrder()
        assertThat(records.map { it.fileName }.distinct()).hasSize(2)
    }

    @Test
    fun theNewestApplicationUpdateComesFirstInTheListOrder() = runTest {
        val ordered = loaded().values.sortedByDescending { it.updatedAt }.map { it.status }

        assertThat(ordered).containsExactly(
            ApplicationStatus.SAVED,
            ApplicationStatus.INTERVIEW,
            ApplicationStatus.APPLIED,
            ApplicationStatus.NO_RESPONSE,
        ).inOrder()
    }

    @Test
    fun theProfileIsPriyaDeshmukhWithOnlyConfirmedFacts() = runTest {
        controller.load()

        val profile = checkNotNull(profiles.observeProfile().first())

        assertThat(profile.fullName).isEqualTo("Priya Deshmukh")
        assertThat(profile.entries.all { it.isConfirmed }).isTrue()
        assertThat(profile.entries.map { it.title }).contains("Placement Stats Dashboard")
    }

    @Test
    fun resetClearsTheExportHistory() = runTest {
        controller.load()

        controller.reset()

        assertThat(store.read("exports.history")).isNull()
    }
}
