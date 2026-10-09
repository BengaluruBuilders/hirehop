package com.tailormyresume.app.billing

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.CreditSpend
import com.tailormyresume.core.domain.PurchaseFailureReason
import com.tailormyresume.core.domain.PurchaseResult
import com.tailormyresume.core.model.CreditKind
import com.tailormyresume.core.network.TailorMyResumeApiConfig
import com.tailormyresume.core.network.tailormyresumeApi
import com.tailormyresume.core.network.tailormyresumeJson
import com.tailormyresume.core.network.tailormyresumeOkHttpClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test

class RemotePaymentGatewayTest {
    private val server = MockWebServer().apply { start() }
    private val billing = FakePlayBilling()
    private val api = tailormyresumeApi(TailorMyResumeApiConfig(server.url("/").toString()), tailormyresumeOkHttpClient(FixedToken), tailormyresumeJson())
    private val source = WalletSource(api)

    @After
    fun tearDown() = server.shutdown()

    private fun gateway(uid: String? = "uid-1") = RemotePaymentGateway(api, source, billing, FakeUid(uid), idleScope())

    private fun reply(code: Int, body: String) = server.enqueue(MockResponse().setResponseCode(code).setBody(body))

    private fun donePurchase(state: PlayPurchaseState = PlayPurchaseState.PURCHASED) {
        billing.purchaseResult = PlayPurchaseResult.Done(PlayPurchase("application_pack_5", "token-1", state))
    }

    @Test
    fun observedEntitlementEmitsNoCreditsWhenTheWalletCannotBeFetched() = runTest {
        server.shutdown()

        val entitlement = gateway().observeEntitlement().first()

        assertThat(entitlement.totalCredits).isEqualTo(0)
    }

    @Test
    fun allowanceFallsBackToTheContractDefaultsWhenTheWalletCannotBeFetched() = runTest {
        server.shutdown()
        val allowance = WalletUsageAllowance(source)

        assertThat(allowance.observeAnalysesLeft().first()).isEqualTo(3)
        assertThat(allowance.observeFreeTailoringsLeft().first()).isEqualTo(1)
    }

    @Test
    fun entitlementComesFromTheWallet() = runTest {
        reply(200, """{"wallet":${walletJson(free = 0, purchased = 4, unlocked = "\"a1\"")}}""")

        val entitlement = gateway().entitlement()

        assertThat(entitlement.purchasedCredits).isEqualTo(4)
        assertThat(entitlement.unlockedApplicationIds).containsExactly("a1")
        assertThat(server.takeRequest().path).isEqualTo("/v1/tailormyresume/wallet")
    }

    @Test
    fun packsUsePlayNameAndPriceAndDropPacksPlayDoesNotKnow() = runTest {
        reply(
            200,
            """{"packs":[{"productId":"application_pack_5","credits":5,"creditsExpire":false},
{"productId":"unknown","credits":9,"creditsExpire":false}]}""",
        )

        val packs = gateway().packs()

        assertThat(packs.map { it.id }).containsExactly("application_pack_5")
        assertThat(packs.single().priceInPaise).isEqualTo(14_900)
        assertThat(packs.single().credits).isEqualTo(5)
    }

    @Test
    fun firstUnlockReportsTheCreditKindAndARepeatReportsNone() = runTest {
        val unlock = """{"unlock":{"applicationId":"a1","creditKind":"FREE","unlockedAt":"2026-10-07T09:00:00Z"},"wallet":${walletJson(free = 0, unlocked = "\"a1\"")}}"""
        reply(201, unlock)
        reply(200, unlock)

        val first = gateway().unlock("a1") as CreditSpend.Spent
        val repeat = gateway().unlock("a1") as CreditSpend.Spent

        assertThat(first.kind).isEqualTo(CreditKind.FREE)
        assertThat(repeat.kind).isNull()
        assertThat(server.takeRequest().path).isEqualTo("/v1/tailormyresume/applications/a1/unlock")
    }

    @Test
    fun unlockWithNoCreditReportsNoCreditLeft() = runTest {
        reply(402, errorJson("NO_CREDIT"))

        assertThat(gateway().unlock("a1")).isEqualTo(CreditSpend.NoCreditLeft)
    }

    @Test
    fun aPurchasedTokenIsPostedWithTheHashedUidAndCompletes() = runTest {
        donePurchase()
        reply(201, purchaseJson(walletJson(free = 1, purchased = 5)))

        val result = gateway("uid-1").purchase("application_pack_5") as PurchaseResult.Completed

        assertThat(result.entitlement.purchasedCredits).isEqualTo(5)
        assertThat(billing.launchedWith).isEqualTo("application_pack_5" to obfuscatedAccountId("uid-1"))
        val request = server.takeRequest()
        assertThat(request.path).isEqualTo("/v1/tailormyresume/purchases")
        assertThat(request.body.readUtf8()).contains("\"purchaseToken\":\"token-1\"")
    }

    @Test
    fun aRepeatedTokenAnswers200AndStillCompletes() = runTest {
        donePurchase()
        reply(200, purchaseJson(walletJson(purchased = 5)))

        assertThat(gateway().purchase("application_pack_5")).isInstanceOf(PurchaseResult.Completed::class.java)
    }

    @Test
    fun serverAnswersAfterPurchasedAreHeldPendingOrUnconfirmed() = runTest {
        donePurchase()
        repeat(3) { reply(502, errorJson("PLAY_UNAVAILABLE")) }
        val held = gateway().purchase("application_pack_5") as PurchaseResult.Pending
        assertThat(held.entitlement.pendingPackIds).containsExactly("application_pack_5")

        listOf(400 to "PURCHASE_INVALID", 403 to "FORBIDDEN").forEach { (status, code) ->
            donePurchase()
            reply(status, errorJson(code))

            val result = gateway().purchase("application_pack_5") as PurchaseResult.Failed

            assertThat(result.reason).isEqualTo(PurchaseFailureReason.PaymentUnconfirmed)
        }
    }

    @Test
    fun aPendingAnswerFromTheServerKeepsThePackPending() = runTest {
        donePurchase()
        reply(409, errorJson("PURCHASE_PENDING"))

        val result = gateway().purchase("application_pack_5") as PurchaseResult.Pending

        assertThat(result.entitlement.pendingPackIds).containsExactly("application_pack_5")
    }

    @Test
    fun aPendingPlayPurchaseIsNeverPostedToTheServer() = runTest {
        donePurchase(PlayPurchaseState.PENDING)

        val result = gateway().purchase("application_pack_5")

        assertThat(result).isInstanceOf(PurchaseResult.Pending::class.java)
        assertThat(server.requestCount).isEqualTo(0)
    }

    @Test
    fun withoutAUidThePurchaseIsUnavailableAndPlayIsNotOpened() = runTest {
        val result = gateway(uid = null).purchase("application_pack_5") as PurchaseResult.Failed

        assertThat(result.reason).isEqualTo(PurchaseFailureReason.PurchaseUnavailable)
        assertThat(billing.launchedWith).isNull()
    }

    @Test
    fun cancellingInPlayCancels() = runTest {
        billing.purchaseResult = PlayPurchaseResult.Cancelled

        assertThat(gateway().purchase("application_pack_5")).isEqualTo(PurchaseResult.Cancelled)
    }

    @Test
    fun restorePostsEveryPurchasedTokenAndSkipsPendingOnes() = runTest {
        billing.owned = listOf(
            PlayPurchase("application_pack_5", "t1", PlayPurchaseState.PURCHASED),
            PlayPurchase("application_pack_5", "t2", PlayPurchaseState.PENDING),
        )
        reply(200, purchaseJson(walletJson(purchased = 5)))
        reply(200, """{"wallet":${walletJson(purchased = 5)}}""")

        val entitlement = gateway().restorePurchases()

        assertThat(entitlement.purchasedCredits).isEqualTo(5)
        assertThat(server.requestCount).isEqualTo(2)
        assertThat(server.takeRequest().body.readUtf8()).contains("\"purchaseToken\":\"t1\"")
    }

    @Test
    fun usageAllowanceReadsTheWallet() = runTest {
        reply(200, """{"wallet":${walletJson(analyses = 2, tailorings = 0)}}""")
        reply(200, """{"wallet":${walletJson(analyses = 2, tailorings = 0)}}""")
        val allowance = WalletUsageAllowance(source)

        assertThat(allowance.consumeFreeTailoring()).isFalse()
        assertThat(allowance.observeAnalysesLeft().first()).isEqualTo(2)
    }

    @Test
    fun consumingAnAllowanceNeverDecrementsLocally() = runTest {
        repeat(3) { reply(200, "{\"wallet\":${walletJson(analyses = 2, tailorings = 1)}}") }
        val allowance = WalletUsageAllowance(source)

        assertThat(allowance.consumeAnalysis()).isTrue()
        assertThat(allowance.consumeFreeTailoring()).isTrue()
        assertThat(allowance.consumeFreeTailoring()).isTrue()
    }
}
