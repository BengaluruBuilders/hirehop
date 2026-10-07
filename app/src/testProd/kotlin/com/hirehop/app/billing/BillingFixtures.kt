package com.hirehop.app.billing

import com.hirehop.core.domain.FirebaseUidProvider
import com.hirehop.core.network.IdTokenProvider

internal class FakePlayBilling : PlayBilling {
    var products = mapOf("application_pack_5" to PlayProduct("5 applications", 149_000_000, "INR"))
    var purchaseResult: PlayPurchaseResult = PlayPurchaseResult.Failed
    var owned = emptyList<PlayPurchase>()
    var launchedWith: Pair<String, String>? = null

    override suspend fun productDetails(productId: String) = products[productId]

    override suspend fun launchPurchase(productId: String, obfuscatedAccountId: String): PlayPurchaseResult {
        launchedWith = productId to obfuscatedAccountId
        return purchaseResult
    }

    override suspend fun ownedPurchases() = owned
}

internal class FakeUid(private val value: String?) : FirebaseUidProvider {
    override fun uid() = value
}

internal object FixedToken : IdTokenProvider {
    override fun idToken(forceRefresh: Boolean) = "test-token"
}

internal fun walletJson(
    free: Int = 1,
    purchased: Int = 0,
    analyses: Int = 3,
    tailorings: Int = 1,
    unlocked: String = "",
) = """{"freeCredits":$free,"purchasedCredits":$purchased,"analysesLeftToday":$analyses,
"freeTailoringsLeftToday":$tailorings,"day":"2026-10-07","resetsAt":"2026-10-07T18:30:00Z",
"unlockedApplicationIds":[$unlocked]}"""

internal fun purchaseJson(wallet: String) =
    """{"purchase":{"orderId":"GPA.1","productId":"application_pack_5","creditsGranted":5,
"purchasedAt":"2026-10-07T09:12:00Z","state":"COMPLETED"},"wallet":$wallet}"""

internal fun errorJson(code: String) = """{"error":{"code":"$code","message":"m"}}"""
