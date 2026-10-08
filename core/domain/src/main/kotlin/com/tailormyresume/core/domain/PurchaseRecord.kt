package com.tailormyresume.core.domain

import kotlin.time.Instant

enum class PurchaseState { PENDING, COMPLETED }

data class PurchaseRecord(
    val packId: String,
    val orderId: String,
    val purchasedAt: Instant,
    val state: PurchaseState,
)
