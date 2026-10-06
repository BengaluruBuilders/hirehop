package com.hirehop.core.domain.offline

import com.hirehop.core.domain.ApplicationPack

object MockPackCatalogue {
    val applicationPackFive = ApplicationPack(
        id = ApplicationPack.APPLICATION_PACK_FIVE,
        name = "Application pack",
        credits = 5,
        priceInPaise = 14_900,
        currencyCode = ApplicationPack.CURRENCY_INR,
        creditsExpire = false,
    )

    val all = listOf(applicationPackFive)
}
