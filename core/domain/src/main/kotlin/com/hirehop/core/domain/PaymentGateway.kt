package com.hirehop.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface PaymentGateway {
    suspend fun packs(): List<ApplicationPack>

    suspend fun purchase(packId: String): PurchaseResult

    suspend fun entitlement(): PurchaseEntitlement

    fun observeEntitlement(): Flow<PurchaseEntitlement> = flow { emit(entitlement()) }

    suspend fun purchaseHistory(): List<PurchaseRecord> = emptyList()

    fun observePurchaseHistory(): Flow<List<PurchaseRecord>> = flow { emit(purchaseHistory()) }

    suspend fun restorePurchases(): PurchaseEntitlement

    suspend fun consumeCredit(): CreditSpend

    suspend fun clearCredits(): PurchaseEntitlement
}
