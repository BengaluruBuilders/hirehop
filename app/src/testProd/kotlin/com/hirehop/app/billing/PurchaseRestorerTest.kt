package com.hirehop.app.billing

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.model.SignInAccount
import com.hirehop.core.network.HirehopApiConfig
import com.hirehop.core.network.hirehopApi
import com.hirehop.core.network.hirehopJson
import com.hirehop.core.network.hirehopOkHttpClient
import com.hirehop.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Test
import java.util.concurrent.TimeUnit

class PurchaseRestorerTest {
    private val server = MockWebServer().apply { start() }
    private val billing = FakePlayBilling().apply { owned = listOf(PlayPurchase("application_pack_5", "token-1", PlayPurchaseState.PURCHASED)) }
    private val session = TestSessionRepository()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val api = hirehopApi(HirehopApiConfig(server.url("/").toString()), hirehopOkHttpClient(FixedToken), hirehopJson())

    @After
    fun tearDown() {
        scope.cancel()
        server.shutdown()
    }

    private fun restorer(): PurchaseRestorer {
        val gateway: PaymentGateway = RemotePaymentGateway(api, WalletSource(api), billing, FakeUid("uid-1"))
        return PurchaseRestorer({ gateway }, { billing }, session, scope)
    }

    private fun reply(code: Int, body: String) = server.enqueue(MockResponse().setResponseCode(code).setBody(body))

    private fun nextRequest(): RecordedRequest = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS)) { "no request arrived" }

    private fun purchaseSucceeds() {
        reply(201, purchaseJson(walletJson(purchased = 5)))
        reply(200, """{"wallet":${walletJson(purchased = 5)}}""")
    }

    @Test
    fun anExistingAccountRestoresThePurchasesAtStart() = runBlocking {
        session.saveAccount(SignInAccount("uid-1", "Priya", "p@example.com"))
        purchaseSucceeds()

        restorer().start()

        assertThat(nextRequest().path).isEqualTo("/v1/hirehop/purchases")
    }

    @Test
    fun signingInAfterStartRestoresThePurchases() = runBlocking {
        purchaseSucceeds()
        restorer().start()
        assertThat(server.requestCount).isEqualTo(0)

        session.saveAccount(SignInAccount("uid-1", "Priya", "p@example.com"))

        assertThat(nextRequest().path).isEqualTo("/v1/hirehop/purchases")
    }

    @Test
    fun anUpdateWithNoPurchaseInFlightIsSettled() = runBlocking {
        purchaseSucceeds()
        restorer().start()
        billing.unsolicitedPurchases.subscriptionCount.first { it > 0 }

        billing.unsolicitedPurchases.emit(billing.owned.single())

        assertThat(nextRequest().path).isEqualTo("/v1/hirehop/purchases")
    }

    @Test
    fun aPurchaseThatFailedToPostIsPostedAgainAtTheNextStart() = runBlocking {
        session.saveAccount(SignInAccount("uid-1", "Priya", "p@example.com"))
        reply(503, """{"error":{"code":"PLAY_UNAVAILABLE","message":"m"}}""")
        reply(500, "")
        restorer().start()
        assertThat(nextRequest().path).isEqualTo("/v1/hirehop/purchases")
        assertThat(nextRequest().path).isEqualTo("/v1/hirehop/wallet")
        purchaseSucceeds()

        restorer().start()

        val retry = nextRequest()
        assertThat(retry.path).isEqualTo("/v1/hirehop/purchases")
        assertThat(retry.body.readUtf8()).contains("\"purchaseToken\":\"token-1\"")
    }
}
