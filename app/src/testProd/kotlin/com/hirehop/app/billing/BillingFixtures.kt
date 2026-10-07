package com.hirehop.app.billing

import com.hirehop.app.ai.RemoteJobAnalysisSource
import com.hirehop.app.auth.NoMatcher
import com.hirehop.app.auth.SignOutCleaner
import com.hirehop.app.auth.api
import com.hirehop.core.data.repository.PendingReportQueue
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.domain.FirebaseUidProvider
import com.hirehop.core.network.IdTokenProvider
import com.hirehop.core.testing.mock.TestMockStateStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockWebServer

internal class FakePlayBilling : PlayBilling {
    var products = mapOf("application_pack_5" to PlayProduct("5 applications", 149_000_000, "INR"))
    var purchaseResult: PlayPurchaseResult = PlayPurchaseResult.Failed
    var owned = emptyList<PlayPurchase>()
    override val unsolicitedPurchases = MutableSharedFlow<PlayPurchase>(extraBufferCapacity = 8)
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

internal fun MockWebServer.signOutCleaner(session: SessionRepository): SignOutCleaner {
    val store = TestMockStateStore()
    val api = api()
    return SignOutCleaner(
        RemotePaymentGateway(api, WalletSource(api), FakePlayBilling(), FakeUid("uid-1")),
        RemoteJobAnalysisSource(api, NoMatcher, Json),
        PendingReportQueue(store),
        store,
        session,
    )
}
