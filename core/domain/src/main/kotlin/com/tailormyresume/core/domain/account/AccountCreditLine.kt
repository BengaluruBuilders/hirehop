package com.tailormyresume.core.domain.account

data class AccountCreditLine(
    val freeCredits: Int,
    val purchasedCredits: Int,
) {
    val total: Int
        get() = freeCredits + purchasedCredits
}
