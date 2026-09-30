package com.hirehop.core.domain

data class PurchaseEntitlement(
    val freeCredits: Int,
    val purchasedCredits: Int,
    val pendingPackIds: List<String>,
) {
    val totalCredits: Int
        get() = freeCredits + purchasedCredits
}
