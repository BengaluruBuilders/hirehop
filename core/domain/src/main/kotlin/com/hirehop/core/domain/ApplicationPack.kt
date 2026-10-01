package com.hirehop.core.domain

data class ApplicationPack(
    val id: String,
    val name: String,
    val credits: Int,
    val priceInPaise: Long,
    val currencyCode: String,
    val creditsExpire: Boolean,
) {
    companion object {
        const val APPLICATION_PACK_FIVE = "application_pack_5"
        const val SINGLE_APPLICATION = "single_application_1"
        const val CURRENCY_INR = "INR"

        val applicationPackFive = ApplicationPack(
            id = APPLICATION_PACK_FIVE,
            name = "Application pack",
            credits = 5,
            priceInPaise = 14_900,
            currencyCode = CURRENCY_INR,
            creditsExpire = false,
        )

        val singleApplication = ApplicationPack(
            id = SINGLE_APPLICATION,
            name = "Single application",
            credits = 1,
            priceInPaise = 4_900,
            currencyCode = CURRENCY_INR,
            creditsExpire = false,
        )

        val catalogue = listOf(applicationPackFive, singleApplication)
    }
}
