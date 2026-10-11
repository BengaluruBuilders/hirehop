package com.tailormyresume.feature.tailor.impl.tailoring

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.ResumeTailor
import com.tailormyresume.core.domain.TailorResumeUseCase
import com.tailormyresume.core.domain.coverage.KeywordCoverageCalculator
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.CreditLedgerKind
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import com.tailormyresume.core.model.TailoredResume
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestCreditsRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.tailor.impl.CleanFabricationGuard
import com.tailormyresume.feature.tailor.impl.entryFor
import com.tailormyresume.feature.tailor.impl.testApplication
import com.tailormyresume.feature.tailor.impl.testBullet
import com.tailormyresume.feature.tailor.impl.testProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TailoringRunnerTest {

    private val dispatcher = StandardTestDispatcher()
    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val creditsRepository = TestCreditsRepository()
    private val clock = TestClock()

    private val requirement = JobRequirement(
        id = "req-1",
        text = "Must have internal tools.",
        type = RequirementType.TOOL,
        priority = RequirementPriority.MUST_HAVE,
        keywords = listOf("internal tool"),
    )

    private val gap = GapAnalysis(
        matches = listOf(RequirementMatch(requirement, MatchStatus.MET, emptyList())),
        keywordCoverage = KeywordCoverage(covered = 1, total = 1),
    )

    private val bullet = testBullet("b1", original = "Built an internal tool", proposed = "Developed an internal tool")

    private val profile = testProfile(listOf(entryFor("exp-1", bullet)))

    private fun seed(gapAnalysis: GapAnalysis? = gap) {
        applicationRepository.sendApplications(listOf(testApplication(emptyList(), gapAnalysis)))
        profileRepository.sendProfile(profile)
    }

    private fun runner(tailor: suspend () -> TailoredResume) = TailoringRunner(
        applicationRepository = applicationRepository,
        profileRepository = profileRepository,
        creditsRepository = creditsRepository,
        tailorResume = TailorResumeUseCase(
            object : ResumeTailor {
                override suspend fun tailor(
                    profile: CandidateProfile,
                    job: JobDescription,
                    gap: GapAnalysis,
                    applicationId: String,
                    section: EntryCategory?,
                ): TailoredResume = tailor()
            },
            CleanFabricationGuard,
        ),
        clock = clock,
        dispatcher = dispatcher,
    )

    @Test
    fun successRecordsExactlyOneSpendAndStoresFinalCoverage() = runTest(dispatcher) {
        seed()
        val tailored = TailoredResume(bullets = listOf(bullet))

        val result = runner { tailored }("app-1")

        assertThat(result).isEqualTo(TailoringResult.Success)
        val ledger = creditsRepository.observeLedger().first()
        assertThat(ledger).hasSize(1)
        assertThat(ledger.single().kind).isEqualTo(CreditLedgerKind.SPEND)
        assertThat(ledger.single().amount).isEqualTo(-1)
        assertThat(ledger.single().applicationId).isEqualTo("app-1")
        val stored = checkNotNull(applicationRepository.observeApplication("app-1").first())
        val storedResume = checkNotNull(stored.tailoredResume)
        assertThat(storedResume.bullets.map { it.id }).containsExactly("b1")
        assertThat(stored.changesAcceptedAt).isNull()
        assertThat(stored.keywordCoverage?.final).isNotNull()
        assertThat(stored.keywordCoverage?.final)
            .isEqualTo(KeywordCoverageCalculator.compute(gap.matches, null, storedResume).final)
    }

    @Test
    fun failureRecordsNoSpendAndLeavesApplicationUnchanged() = runTest(dispatcher) {
        seed()
        val before = applicationRepository.observeApplication("app-1").first()

        val result = runner { error("model unavailable") }("app-1")

        assertThat(result).isInstanceOf(TailoringResult.Failure::class.java)
        assertThat(creditsRepository.observeLedger().first()).isEmpty()
        assertThat(applicationRepository.observeApplication("app-1").first()).isEqualTo(before)
    }

    @Test
    fun missingGapAnalysisFailsWithoutSpend() = runTest(dispatcher) {
        seed(gapAnalysis = null)
        val before = applicationRepository.observeApplication("app-1").first()

        val result = runner { TailoredResume(bullets = emptyList()) }("app-1")

        assertThat(result).isInstanceOf(TailoringResult.Failure::class.java)
        assertThat(creditsRepository.observeLedger().first()).isEmpty()
        assertThat(applicationRepository.observeApplication("app-1").first()).isEqualTo(before)
    }

    @Test
    fun cancelledTailoringRecordsNoSpend() = runTest(dispatcher) {
        seed()
        val started = CompletableDeferred<Unit>()
        val run = runner {
            started.complete(Unit)
            awaitCancellation()
        }

        val job = launch { run("app-1") }
        runCurrent()
        started.await()
        job.cancel()
        runCurrent()

        assertThat(job.isCancelled).isTrue()
        assertThat(creditsRepository.observeLedger().first()).isEmpty()
        assertThat(checkNotNull(applicationRepository.observeApplication("app-1").first()).tailoredResume)
            .isEqualTo(TailoredResume(emptyList()))
    }

    @Test
    fun cancellationIsNotReportedAsFailure() = runTest(dispatcher) {
        seed()
        var thrown: Throwable? = null
        val job = launch {
            try {
                runner { throw CancellationException("stop") }("app-1")
            } catch (expected: CancellationException) {
                thrown = expected
            }
        }
        runCurrent()
        job.join()

        assertThat(thrown).isInstanceOf(CancellationException::class.java)
        assertThat(creditsRepository.observeLedger().first()).isEmpty()
    }

    private val priorSpend = com.tailormyresume.core.model.CreditLedgerEntry(
        CreditLedgerKind.SPEND,
        -1,
        "app-1",
        null,
        clock.now(),
    )

    @Test
    fun restoredAfterFinishNeitherRerunsNorSpendsAgain() = runTest(dispatcher) {
        seed()
        val tailored = TailoredResume(bullets = listOf(bullet))
        runner { tailored }("app-1")
        var reruns = 0

        val result = runner {
            reruns++
            tailored
        }("app-1")

        assertThat(result).isEqualTo(TailoringResult.Success)
        assertThat(reruns).isEqualTo(0)
        assertThat(creditsRepository.observeLedger().first()).hasSize(1)
    }

    @Test
    fun crashBetweenSpendAndResultStillProducesTheResultWithoutASecondSpend() = runTest(dispatcher) {
        seed()
        creditsRepository.sendLedger(listOf(priorSpend))
        val tailored = TailoredResume(bullets = listOf(bullet))

        val result = runner { tailored }("app-1")

        assertThat(result).isEqualTo(TailoringResult.Success)
        assertThat(creditsRepository.observeLedger().first()).hasSize(1)
        val stored = checkNotNull(applicationRepository.observeApplication("app-1").first())
        assertThat(stored.tailoredResume?.bullets?.map { it.id }).containsExactly("b1")
        assertThat(stored.keywordCoverage?.final).isNotNull()
    }
}
