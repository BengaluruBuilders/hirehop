package com.hirehop.core.domain

interface PaymentGateway {
    suspend fun packs(): List<ApplicationPack>

    suspend fun purchase(packId: String): PurchaseResult

    suspend fun entitlement(): PurchaseEntitlement

    suspend fun restorePurchases(): PurchaseEntitlement

    suspend fun consumeCredit(): CreditSpend
}
