package com.tailormyresume.core.domain.account

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.PendingWipeState
import com.tailormyresume.core.testing.repository.TestPendingAccountWipe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FinishPendingAccountWipeUseCaseTest {

    private val marker = TestPendingAccountWipe()
    private val finisher = CountingFinisher()
    private var closedProbe: Result<Boolean> = Result.success(true)
    private var probes = 0

    private val deleter = object : ServerAccountDeleter {
        override val deletesRemoteData = true

        override suspend fun delete() = Result.success(Unit)

        override suspend fun isClosed(): Result<Boolean> {
            probes++
            return closedProbe
        }
    }

    private fun useCase() = FinishPendingAccountWipeUseCase(marker, finisher, deleter)

    @Test
    fun noMarkerDoesNothing() = runTest {
        val outcome = useCase()()

        assertThat(outcome).isEqualTo(PendingWipeOutcome.NOTHING_PENDING)
        assertThat(finisher.runs).isEqualTo(0)
        assertThat(probes).isEqualTo(0)
    }

    @Test
    fun serverClosedRunsFinisherThenClears() = runTest {
        marker.current = PendingWipeState.SERVER_CLOSED

        val outcome = useCase()()

        assertThat(outcome).isEqualTo(PendingWipeOutcome.FINISHED)
        assertThat(finisher.runs).isEqualTo(1)
        assertThat(marker.current).isEqualTo(PendingWipeState.NONE)
        assertThat(probes).isEqualTo(0)
    }

    @Test
    fun finisherFailureKeepsMarker() = runTest {
        marker.current = PendingWipeState.SERVER_CLOSED
        finisher.failuresLeft = 1

        val outcome = useCase()()

        assertThat(outcome).isEqualTo(PendingWipeOutcome.STILL_PENDING)
        assertThat(marker.current).isEqualTo(PendingWipeState.SERVER_CLOSED)
    }

    @Test
    fun serverClosedWithTheWipeThrowingOnceFinishesAfterARestart() = runTest {
        marker.current = PendingWipeState.SERVER_CLOSED
        finisher.failuresLeft = 1
        useCase()()

        val outcome = useCase()()

        assertThat(outcome).isEqualTo(PendingWipeOutcome.FINISHED)
        assertThat(finisher.runs).isEqualTo(2)
        assertThat(marker.current).isEqualTo(PendingWipeState.NONE)
    }

    @Test
    fun requestedAndServerOpenClearsMarkerKeepsData() = runTest {
        marker.current = PendingWipeState.REQUESTED
        closedProbe = Result.success(false)

        val outcome = useCase()()

        assertThat(outcome).isEqualTo(PendingWipeOutcome.ACCOUNT_KEPT)
        assertThat(finisher.runs).isEqualTo(0)
        assertThat(marker.current).isEqualTo(PendingWipeState.NONE)
    }

    @Test
    fun requestedAndServerClosedWipes() = runTest {
        marker.current = PendingWipeState.REQUESTED
        closedProbe = Result.success(true)

        val outcome = useCase()()

        assertThat(outcome).isEqualTo(PendingWipeOutcome.FINISHED)
        assertThat(finisher.runs).isEqualTo(1)
        assertThat(marker.current).isEqualTo(PendingWipeState.NONE)
    }

    @Test
    fun requestedAndServerClosedWithTheWipeThrowingKeepsTheClosedMarker() = runTest {
        marker.current = PendingWipeState.REQUESTED
        finisher.failuresLeft = 1

        val outcome = useCase()()

        assertThat(outcome).isEqualTo(PendingWipeOutcome.STILL_PENDING)
        assertThat(marker.current).isEqualTo(PendingWipeState.SERVER_CLOSED)
    }

    @Test
    fun requestedOfflineLeavesMarker() = runTest {
        marker.current = PendingWipeState.REQUESTED
        closedProbe = Result.failure(java.io.IOException("offline"))

        val outcome = useCase()()

        assertThat(outcome).isEqualTo(PendingWipeOutcome.STILL_PENDING)
        assertThat(finisher.runs).isEqualTo(0)
        assertThat(marker.current).isEqualTo(PendingWipeState.REQUESTED)
    }

    @Test
    fun runsTwiceSafely() = runTest {
        marker.current = PendingWipeState.SERVER_CLOSED
        val useCase = useCase()

        useCase()
        val second = useCase()

        assertThat(second).isEqualTo(PendingWipeOutcome.NOTHING_PENDING)
        assertThat(finisher.runs).isEqualTo(1)
    }

    @Test
    fun concurrentCallsRunOnce() = runTest(StandardTestDispatcher()) {
        marker.current = PendingWipeState.SERVER_CLOSED
        finisher.hold = CompletableDeferred()
        val useCase = useCase()

        val calls = List(3) { async { useCase() } }
        advanceUntilIdle()
        finisher.hold?.complete(Unit)
        val outcomes = calls.awaitAll()

        assertThat(finisher.runs).isEqualTo(1)
        assertThat(outcomes.count { it == PendingWipeOutcome.FINISHED }).isEqualTo(1)
        assertThat(marker.current).isEqualTo(PendingWipeState.NONE)
    }

    private class CountingFinisher : AccountWipeFinisher {
        var runs = 0
        var failuresLeft = 0
        var hold: CompletableDeferred<Unit>? = null

        override suspend fun finish() {
            runs++
            hold?.await()
            if (failuresLeft > 0) {
                failuresLeft--
                throw IllegalStateException("wipe failed")
            }
        }
    }
}
