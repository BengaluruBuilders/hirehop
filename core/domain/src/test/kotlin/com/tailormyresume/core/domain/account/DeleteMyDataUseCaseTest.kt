package com.tailormyresume.core.domain.account

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.CreditKind
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.model.ExportRecord
import com.tailormyresume.core.model.KeptJobDescription
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class DeleteMyDataUseCaseTest {

    private val applications = TestApplicationRepository().apply {
        sendApplications(listOf(canonicalApplication, canonicalApplication.copy(id = "application-second")))
    }
    private val profiles = TestProfileRepository().apply { sendProfile(canonicalCandidateProfile) }
    private val exportHistory = TestExportHistoryRepository().apply {
        sendExports(
            listOf(
                ExportRecord(
                    applicationId = canonicalApplication.id,
                    format = ExportFormat.PDF,
                    fileName = "resume.pdf",
                    exportedAt = Instant.fromEpochSeconds(1),
                    creditKind = CreditKind.FREE,
                ),
            ),
        )
    }
    private val session = TestSessionRepository()
    private var exportedFilesDeletions = 0
    private var transientClears = 0
    private var failingApplicationIds: Set<String> = emptySet()
    private var failingExportedFiles = false

    private val failingApplications = object : ApplicationRepository by applications {
        override suspend fun deleteApplication(id: String) {
            if (id in failingApplicationIds) throw IllegalStateException("disk error")
            applications.deleteApplication(id)
        }
    }

    private fun useCase() = DeleteMyDataUseCase(
        applicationRepository = failingApplications,
        profileRepository = profiles,
        exportHistoryRepository = exportHistory,
        sessionRepository = session,
        exportedFiles = ExportedFiles {
            if (failingExportedFiles) throw IllegalStateException("cache locked")
            exportedFilesDeletions++
        },
        transientData = TransientDataCleaner { transientClears++ },
    )

    private suspend fun keepAccountState() {
        session.saveAccount(SignInAccount.localAccount)
        session.recordConsent(ConsentRecord(setOf(ConsentPurpose.AI_PROCESSING), Instant.fromEpochSeconds(1), "2026-10-b"))
        session.markOnboardingComplete()
        session.keepJobDescription(KeptJobDescription(text = "jd", company = "Acme", role = "Analyst"))
    }

    @Test
    fun deletesTheProfileApplicationsExportsKeptJobAndFiles() = runTest {
        keepAccountState()

        val result = useCase()()

        assertThat(result.isSuccess).isTrue()
        assertThat(profiles.observeProfile().first()).isNull()
        assertThat(applications.observeApplications().first()).isEmpty()
        assertThat(exportHistory.observeExports().first()).isEmpty()
        assertThat(session.observeKeptJobDescription().first()).isNull()
        assertThat(exportedFilesDeletions).isEqualTo(1)
        assertThat(transientClears).isEqualTo(1)
    }

    @Test
    fun keepsTheAccountConsentAndOnboardingFlag() = runTest {
        keepAccountState()

        useCase()()

        assertThat(session.observeAccount().first()).isEqualTo(SignInAccount.localAccount)
        assertThat(session.observeConsent().first()).isNotNull()
        assertThat(session.observeOnboardingComplete().first()).isTrue()
    }

    @Test
    fun whenAnApplicationFailsToDelete_reportsTheFailureAndKeepsTheRest() = runTest {
        failingApplicationIds = setOf("application-second")

        val result = useCase()()

        assertThat(result.isFailure).isTrue()
        assertThat(applications.observeApplications().first().map { it.id }).containsExactly("application-second")
    }

    @Test
    fun retryingAfterAFailureFinishesTheDeletion() = runTest {
        failingApplicationIds = setOf("application-second")
        useCase()()
        failingApplicationIds = emptySet()

        val retry = useCase()()

        assertThat(retry.isSuccess).isTrue()
        assertThat(applications.observeApplications().first()).isEmpty()
        assertThat(profiles.observeProfile().first()).isNull()
        assertThat(exportedFilesDeletions).isEqualTo(1)
    }

    @Test
    fun whenTheExportedFilesFail_reportsTheFailureAndRetryCompletes() = runTest {
        failingExportedFiles = true

        val failed = useCase()()
        failingExportedFiles = false
        val retry = useCase()()

        assertThat(failed.isFailure).isTrue()
        assertThat(retry.isSuccess).isTrue()
        assertThat(exportedFilesDeletions).isEqualTo(1)
    }

    @Test
    fun runningTwiceOnEmptyDataSucceeds() = runTest {
        useCase()()

        assertThat(useCase()().isSuccess).isTrue()
    }
}
