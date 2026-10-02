package com.hirehop.core.domain

import com.hirehop.core.model.CreditKind

sealed interface CreditSpend {
    data class Spent(
        val entitlement: PurchaseEntitlement,
        val kind: CreditKind = CreditKind.FREE,
    ) : CreditSpend

    data object NoCreditLeft : CreditSpend
}
