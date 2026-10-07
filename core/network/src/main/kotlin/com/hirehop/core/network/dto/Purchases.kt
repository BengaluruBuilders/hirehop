package com.hirehop.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseRequest(val productId: String, val purchaseToken: String)

@Serializable
data class PurchaseDto(
    val orderId: String,
    val productId: String,
    val creditsGranted: Int,
    val purchasedAt: String,
    val state: String,
)

@Serializable
data class PurchaseResponse(val purchase: PurchaseDto, val wallet: WalletDto)

@Serializable
data class PurchasesResponse(val purchases: List<PurchaseDto>)
