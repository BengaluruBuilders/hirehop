package com.tailormyresume.core.data.repository

import com.tailormyresume.core.data.mock.MockStateStore
import com.tailormyresume.core.data.mock.observeValue
import com.tailormyresume.core.data.mock.readValue
import com.tailormyresume.core.data.mock.writeValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class StoredUsageAllowance @Inject constructor(
    private val store: MockStateStore,
    private val clock: Clock,
) : UsageAllowance {

    private val mutex = Mutex()

    override fun observeAnalysesLeft(): Flow<Int> =
        observeUsage().map { UsageAllowance.DAILY_ANALYSES - it.analyses }.distinctUntilChanged()

    override fun observeFreeTailoringsLeft(): Flow<Int> =
        observeUsage().map { UsageAllowance.DAILY_FREE_TAILORINGS - it.tailorings }.distinctUntilChanged()

    override suspend fun consumeAnalysis(): Boolean = consume(UsageAllowance.DAILY_ANALYSES, { it.analyses }) {
        it.copy(analyses = it.analyses + 1)
    }

    override suspend fun consumeFreeTailoring(): Boolean =
        consume(UsageAllowance.DAILY_FREE_TAILORINGS, { it.tailorings }) { it.copy(tailorings = it.tailorings + 1) }

    override suspend fun clear() {
        mutex.withLock { store.remove(USAGE_KEY) }
    }

    private suspend fun consume(limit: Int, used: (UsageDto) -> Int, next: (UsageDto) -> UsageDto): Boolean =
        mutex.withLock {
            val today = currentUsage(store.readValue(USAGE_KEY, UsageDto.serializer()))
            val allowed = used(today) < limit
            if (allowed) store.writeValue(USAGE_KEY, UsageDto.serializer(), next(today))
            allowed
        }

    private fun observeUsage(): Flow<UsageDto> =
        store.observeValue(USAGE_KEY, UsageDto.serializer()).map { currentUsage(it) }

    private fun currentUsage(stored: UsageDto?): UsageDto {
        val day = UsageDay.of(clock.now())
        return if (stored != null && stored.day == day) stored else UsageDto(day = day)
    }

    private companion object {
        const val USAGE_KEY = "usage.allowance"
    }
}

@Serializable
private data class UsageDto(val day: Long, val analyses: Int = 0, val tailorings: Int = 0)
