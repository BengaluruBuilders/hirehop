package com.hirehop.app.billing

import com.hirehop.core.data.repository.UsageAllowance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletUsageAllowance @Inject constructor(private val source: WalletSource) : UsageAllowance {
    override fun observeAnalysesLeft(): Flow<Int> =
        observed().map { it.analysesLeftToday.coerceAtLeast(0) }.distinctUntilChanged()

    override fun observeFreeTailoringsLeft(): Flow<Int> =
        observed().map { it.freeTailoringsLeftToday.coerceAtLeast(0) }.distinctUntilChanged()

    override suspend fun consumeAnalysis(): Boolean {
        source.refreshOrCached()
        return true
    }

    override suspend fun consumeFreeTailoring(): Boolean = (source.refreshOrCached()?.freeTailoringsLeftToday ?: 0) > 0

    override suspend fun clear() = source.clear()

    private fun observed() = source.wallet.onStart { source.refreshOrCached() }
}
