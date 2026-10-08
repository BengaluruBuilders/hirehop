package com.tailormyresume.app.billing

import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.CreditSpend
import com.tailormyresume.core.domain.FirebaseUidProvider
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.domain.PurchaseEntitlement
import com.tailormyresume.core.domain.PurchaseFailureReason
import com.tailormyresume.core.domain.PurchaseRecord
import com.tailormyresume.core.domain.PurchaseResult
import com.tailormyresume.core.domain.PurchaseState
import com.tailormyresume.core.model.CreditKind
import com.tailormyresume.core.network.ApiError
import com.tailormyresume.core.network.ApiException
import com.tailormyresume.core.network.TailorMyResumeApi
import com.tailormyresume.core.network.apiResult
import com.tailormyresume.core.network.dto.PurchaseRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

@Singleton
class RemotePaymentGateway @Inject constructor(
    private val api: TailorMyResumeApi,
    private val wallet: WalletSource,
    private val billing: PlayBilling,
    private val uids: FirebaseUidProvider,
) : PaymentGateway {
    private val pending = MutableStateFlow<List<String>>(emptyList())

    override suspend fun packs(): List<ApplicationPack> = apiResult { api.packs().packs }.getOrThrow().mapNotNull { pack ->
        billing.productDetails(pack.productId)?.let { product ->
            ApplicationPack(
                id = pack.productId,
                name = product.name,
                credits = pack.credits,
                priceInPaise = product.priceMicros / MICROS_PER_PAISE,
                currencyCode = product.currencyCode,
                creditsExpire = pack.creditsExpire,
            )
        }
    }

    override suspend fun entitlement(): PurchaseEntitlement = wallet.refresh().toEntitlement(pending.value)

    override fun observeEntitlement(): Flow<PurchaseEntitlement> =
        combine(wallet.wallet, pending) { current, held ->
            current?.toEntitlement(held) ?: NO_CREDITS.copy(pendingPackIds = held)
        }
            .onStart {
                emit(current())
                wallet.refreshOrCached()
            }

    override suspend fun purchaseHistory(): List<PurchaseRecord> =
        apiResult { api.purchases().purchases }.getOrThrow().map { purchase ->
            PurchaseRecord(purchase.productId, purchase.orderId, Instant.parse(purchase.purchasedAt), PurchaseState.COMPLETED)
        }

    override suspend fun purchase(packId: String): PurchaseResult {
        val uid = uids.uid() ?: return failed(PurchaseFailureReason.PurchaseUnavailable)
        return when (val outcome = billing.launchPurchase(packId, obfuscatedAccountId(uid))) {
            PlayPurchaseResult.Cancelled -> PurchaseResult.Cancelled
            PlayPurchaseResult.Failed -> failed(PurchaseFailureReason.PaymentUnavailable)
            PlayPurchaseResult.AlreadyOwned -> settleOwned(packId)
            is PlayPurchaseResult.Done -> settle(outcome.purchase, SETTLE_RETRIES)
        }
    }

    override suspend fun restorePurchases(): PurchaseEntitlement {
        billing.ownedPurchases().forEach { owned -> settle(owned, retries = 0) }
        return entitlement()
    }

    override suspend fun unlock(applicationId: String): CreditSpend {
        val result = apiResult {
            api.unlock(applicationId).also { response -> if (!response.isSuccessful) throw HttpException(response) }
        }
        val response = result.getOrElse { failure ->
            if ((failure as? ApiException)?.error == ApiError.NoCredit) return CreditSpend.NoCreditLeft
            throw failure
        }
        val body = checkNotNull(response.body())
        wallet.update(body.wallet)
        val kind = if (response.code() == HTTP_CREATED) CreditKind.valueOf(body.unlock.creditKind.name) else null
        return CreditSpend.Spent(body.wallet.toEntitlement(pending.value), kind)
    }

    override suspend fun clearCredits(): PurchaseEntitlement {
        wallet.clear()
        pending.value = emptyList()
        return NO_CREDITS
    }

    private suspend fun settleOwned(packId: String): PurchaseResult {
        val owned = billing.ownedPurchases().firstOrNull { it.productId == packId }
            ?: return failed(PurchaseFailureReason.PaymentUnavailable)
        return settle(owned, SETTLE_RETRIES)
    }

    private suspend fun settle(purchase: PlayPurchase, retries: Int): PurchaseResult {
        if (purchase.state == PlayPurchaseState.PENDING) return hold(purchase.productId)
        val result = apiResult { api.purchase(PurchaseRequest(purchase.productId, purchase.token)) }
        val response = result.getOrElse { failure ->
            if (retries > 0 && failure.isRecordingFailure()) {
                delay(RETRY_DELAY_MILLIS * (SETTLE_RETRIES - retries + 1))
                return settle(purchase, retries - 1)
            }
            return failureResult(failure, purchase.productId)
        }
        wallet.update(response.wallet)
        pending.value -= purchase.productId
        return PurchaseResult.Completed(response.wallet.toEntitlement(pending.value))
    }

    private fun Throwable.isRecordingFailure() = (this as? ApiException)?.error !in SERVER_ANSWERS

    private fun failureResult(failure: Throwable, productId: String): PurchaseResult = when ((failure as? ApiException)?.error) {
        ApiError.PurchaseInvalid, ApiError.Forbidden -> failed(PurchaseFailureReason.PaymentUnconfirmed)
        else -> hold(productId)
    }

    private fun hold(productId: String): PurchaseResult {
        if (productId !in pending.value) pending.value += productId
        return PurchaseResult.Pending(current())
    }

    private fun failed(reason: PurchaseFailureReason) = PurchaseResult.Failed(reason, current())

    private fun current() = wallet.cached?.toEntitlement(pending.value) ?: NO_CREDITS.copy(pendingPackIds = pending.value)

    private companion object {
        const val MICROS_PER_PAISE = 10_000L
        const val HTTP_CREATED = 201
        const val SETTLE_RETRIES = 2
        val SERVER_ANSWERS = setOf(ApiError.PurchasePending, ApiError.PurchaseInvalid, ApiError.Forbidden)
        const val RETRY_DELAY_MILLIS = 1_000L
    }
}
