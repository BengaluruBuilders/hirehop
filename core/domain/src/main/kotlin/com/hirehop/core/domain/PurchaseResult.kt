package com.hirehop.core.domain

sealed interface PurchaseResult {
    data class Completed(val entitlement: PurchaseEntitlement) : PurchaseResult

    data class Pending(val entitlement: PurchaseEntitlement) : PurchaseResult

    data object Cancelled : PurchaseResult

    data class Failed(
        val reason: PurchaseFailureReason,
        val entitlement: PurchaseEntitlement,
    ) : PurchaseResult
}
