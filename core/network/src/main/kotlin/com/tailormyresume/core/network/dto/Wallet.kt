package com.tailormyresume.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class WalletDto(
    val credits: Int = 0,
    val freeCredits: Int,
    val purchasedCredits: Int,
    val analysesLeftToday: Int,
    val freeTailoringsLeftToday: Int,
    val day: String,
    val resetsAt: String,
    val unlockedApplicationIds: List<String>,
)

@Serializable
data class WalletResponse(val wallet: WalletDto)

@Serializable
enum class CreditKind { FREE, PURCHASED }

@Serializable
data class UnlockDto(val applicationId: String, val creditKind: CreditKind, val unlockedAt: String)

@Serializable
data class UnlockResponse(val unlock: UnlockDto, val wallet: WalletDto)

@Serializable
data class PackDto(val productId: String, val credits: Int, val creditsExpire: Boolean)

@Serializable
data class PacksResponse(val packs: List<PackDto>)
