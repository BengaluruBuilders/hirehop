package com.tailormyresume.core.testing.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.UsageAllowance
import com.tailormyresume.core.testing.util.TestClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.TimeZone
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

abstract class UsageAllowanceContractTest {

    protected abstract fun createUsageAllowance(clock: TestClock): UsageAllowance

    private val clock = TestClock(Instant.parse("2026-10-02T09:00:00Z"))
    private val originalZone: TimeZone = TimeZone.getDefault()

    @Before
    fun useUtc() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun restoreZone() {
        TimeZone.setDefault(originalZone)
    }

    @Test
    fun aFreshDayStartsWithTheFullAllowance() = runTest {
        val allowance = createUsageAllowance(clock)

        assertThat(allowance.observeAnalysesLeft().first()).isEqualTo(UsageAllowance.DAILY_ANALYSES)
        assertThat(allowance.observeFreeTailoringsLeft().first()).isEqualTo(UsageAllowance.DAILY_FREE_TAILORINGS)
    }

    @Test
    fun eachAnalysisUsesOneOfThree() = runTest {
        val allowance = createUsageAllowance(clock)

        assertThat(allowance.consumeAnalysis()).isTrue()

        assertThat(allowance.observeAnalysesLeft().first()).isEqualTo(2)
    }

    @Test
    fun theFourthAnalysisOfTheDayIsRefused() = runTest {
        val allowance = createUsageAllowance(clock)
        repeat(UsageAllowance.DAILY_ANALYSES) { allowance.consumeAnalysis() }

        assertThat(allowance.consumeAnalysis()).isFalse()
        assertThat(allowance.observeAnalysesLeft().first()).isEqualTo(0)
    }

    @Test
    fun theSecondFreeTailoringOfTheDayIsRefused() = runTest {
        val allowance = createUsageAllowance(clock)

        assertThat(allowance.consumeFreeTailoring()).isTrue()
        assertThat(allowance.consumeFreeTailoring()).isFalse()
        assertThat(allowance.observeFreeTailoringsLeft().first()).isEqualTo(0)
    }

    @Test
    fun analysesAndTailoringsAreCountedApart() = runTest {
        val allowance = createUsageAllowance(clock)

        allowance.consumeFreeTailoring()

        assertThat(allowance.observeAnalysesLeft().first()).isEqualTo(UsageAllowance.DAILY_ANALYSES)
    }

    @Test
    fun theAllowanceStaysUsedLaterOnTheSameDay() = runTest {
        val allowance = createUsageAllowance(clock)
        allowance.consumeFreeTailoring()

        clock.instant = clock.instant + 5.hours

        assertThat(allowance.observeFreeTailoringsLeft().first()).isEqualTo(0)
    }

    @Test
    fun theAllowanceResetsOnTheNextLocalDay() = runTest {
        val allowance = createUsageAllowance(clock)
        repeat(UsageAllowance.DAILY_ANALYSES) { allowance.consumeAnalysis() }
        allowance.consumeFreeTailoring()

        clock.instant = clock.instant + 16.hours

        assertThat(allowance.observeAnalysesLeft().first()).isEqualTo(UsageAllowance.DAILY_ANALYSES)
        assertThat(allowance.observeFreeTailoringsLeft().first()).isEqualTo(UsageAllowance.DAILY_FREE_TAILORINGS)
        assertThat(allowance.consumeAnalysis()).isTrue()
    }

    @Test
    fun clearRestoresTheFullAllowance() = runTest {
        val allowance = createUsageAllowance(clock)
        allowance.consumeAnalysis()
        allowance.consumeFreeTailoring()

        allowance.clear()

        assertThat(allowance.observeAnalysesLeft().first()).isEqualTo(UsageAllowance.DAILY_ANALYSES)
        assertThat(allowance.observeFreeTailoringsLeft().first()).isEqualTo(UsageAllowance.DAILY_FREE_TAILORINGS)
    }
}
