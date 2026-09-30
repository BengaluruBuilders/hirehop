package com.hirehop.core.domain

sealed interface CreditSpend {
    data class Spent(val entitlement: PurchaseEntitlement) : CreditSpend

    data object NoCreditLeft : CreditSpend
}
