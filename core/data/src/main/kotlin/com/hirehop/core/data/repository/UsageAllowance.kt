package com.hirehop.core.data.repository

import kotlinx.coroutines.flow.Flow
import java.util.TimeZone
import kotlin.time.Instant

interface UsageAllowance {
    fun observeAnalysesLeft(): Flow<Int>

    fun observeFreeTailoringsLeft(): Flow<Int>

    suspend fun consumeAnalysis(): Boolean

    suspend fun consumeFreeTailoring(): Boolean

    suspend fun clear()

    companion object {
        const val DAILY_ANALYSES: Int = 3
        const val DAILY_FREE_TAILORINGS: Int = 1
    }
}

object UsageDay {
    private const val MILLIS_PER_DAY = 86_400_000L

    fun of(instant: Instant, zone: TimeZone = TimeZone.getDefault()): Long {
        val millis = instant.toEpochMilliseconds()
        return Math.floorDiv(millis + zone.getOffset(millis), MILLIS_PER_DAY)
    }
}
