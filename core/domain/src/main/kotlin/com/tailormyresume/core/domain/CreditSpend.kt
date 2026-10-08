package com.tailormyresume.core.domain

import com.tailormyresume.core.model.CreditKind

sealed interface CreditSpend {
    data class Spent(
        val entitlement: PurchaseEntitlement,
        val kind: CreditKind? = CreditKind.FREE,
    ) : CreditSpend

    data object NoCreditLeft : CreditSpend
}
