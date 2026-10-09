package com.tailormyresume.core.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

interface PaymentGateway {
    suspend fun packs(): List<ApplicationPack>

    suspend fun purchase(packId: String): PurchaseResult

    suspend fun entitlement(): PurchaseEntitlement

    fun observeEntitlement(): Flow<PurchaseEntitlement> = flow { emit(entitlement()) }

    suspend fun purchaseHistory(): List<PurchaseRecord> = emptyList()

    fun observePurchaseHistory(): Flow<List<PurchaseRecord>> = flow { emit(purchaseHistory()) }

    suspend fun restorePurchases(): PurchaseEntitlement

    suspend fun unlock(applicationId: String): CreditSpend

    suspend fun clearCredits(): PurchaseEntitlement
}

fun PaymentGateway.observePurchaseHistoryOrEmpty(): Flow<List<PurchaseRecord>> =
    observePurchaseHistory().catch { failure ->
        if (failure is CancellationException) throw failure
        emit(emptyList())
    }
