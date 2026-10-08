package com.tailormyresume.core.testing.repository

import com.tailormyresume.core.data.repository.UsageAllowance
import com.tailormyresume.core.data.repository.UsageDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Clock

class TestUsageAllowance(private val clock: Clock) : UsageAllowance {

    private data class Usage(val day: Long, val analyses: Int = 0, val tailorings: Int = 0)

    private val usage = MutableStateFlow(Usage(UsageDay.of(clock.now())))

    override fun observeAnalysesLeft(): Flow<Int> =
        usage.map { UsageAllowance.DAILY_ANALYSES - today(it).analyses }.distinctUntilChanged()

    override fun observeFreeTailoringsLeft(): Flow<Int> =
        usage.map { UsageAllowance.DAILY_FREE_TAILORINGS - today(it).tailorings }.distinctUntilChanged()

    override suspend fun consumeAnalysis(): Boolean = consume { current ->
        if (current.analyses < UsageAllowance.DAILY_ANALYSES) current.copy(analyses = current.analyses + 1) else null
    }

    override suspend fun consumeFreeTailoring(): Boolean = consume { current ->
        if (current.tailorings < UsageAllowance.DAILY_FREE_TAILORINGS) current.copy(tailorings = current.tailorings + 1) else null
    }

    override suspend fun clear() {
        usage.value = Usage(UsageDay.of(clock.now()))
    }

    private fun consume(next: (Usage) -> Usage?): Boolean {
        var allowed = false
        usage.update { stored ->
            val updated = next(today(stored))
            allowed = updated != null
            updated ?: today(stored)
        }
        return allowed
    }

    private fun today(stored: Usage): Usage {
        val day = UsageDay.of(clock.now())
        return if (stored.day == day) stored else Usage(day)
    }
}
