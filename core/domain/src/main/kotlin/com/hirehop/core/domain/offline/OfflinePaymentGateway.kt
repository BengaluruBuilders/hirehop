package com.hirehop.core.domain.offline

import com.hirehop.core.data.mock.MockLatency
import com.hirehop.core.data.mock.MockOperation
import com.hirehop.core.data.mock.MockStateStore
import com.hirehop.core.data.mock.observeValue
import com.hirehop.core.data.mock.readValue
import com.hirehop.core.data.mock.writeValue
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.CreditSpend
import com.hirehop.core.domain.IdGenerator
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.domain.PurchaseOutcome
import com.hirehop.core.domain.PurchaseRecord
import com.hirehop.core.domain.PurchaseResult
import com.hirehop.core.model.CreditKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

@Singleton
class OfflinePaymentGateway @Inject constructor(
    private val store: MockStateStore,
    private val latency: MockLatency,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) : PaymentGateway {

    private val mutex = Mutex()
    private val scriptedOutcomes = mutableMapOf<String, PurchaseOutcome>()
    private var failureReason: PurchaseFailureReason = PurchaseFailureReason.PaymentUnavailable
    private var startingFreeCredits: Int = DEFAULT_FREE_CREDITS

    fun withOutcome(
        packId: String,
        outcome: PurchaseOutcome,
    ): OfflinePaymentGateway = apply { scriptedOutcomes[packId] = outcome }

    fun withFailureReason(reason: PurchaseFailureReason): OfflinePaymentGateway = apply {
        failureReason = reason
    }

    fun withFreeCredits(credits: Int): OfflinePaymentGateway = apply { startingFreeCredits = credits }

    override suspend fun packs(): List<ApplicationPack> {
        latency.await(MockOperation.LOAD_PACKS)
        return MockPackCatalogue.all
    }

    override suspend fun purchase(packId: String): PurchaseResult {
        latency.await(MockOperation.PURCHASE)
        return mutex.withLock {
            val state = load()
            val pack = MockPackCatalogue.all.find { candidate -> candidate.id == packId }
            if (pack == null) {
                PurchaseResult.Failed(PurchaseFailureReason.PurchaseUnavailable, state.toEntitlement())
            } else {
                resolve(state, pack)
            }
        }
    }

    override suspend fun entitlement(): PurchaseEntitlement = mutex.withLock { load().toEntitlement() }

    override fun observeEntitlement(): Flow<PurchaseEntitlement> =
        observeState().map(PaymentState::toEntitlement).distinctUntilChanged()

    override suspend fun purchaseHistory(): List<PurchaseRecord> = mutex.withLock { load().history() }

    override fun observePurchaseHistory(): Flow<List<PurchaseRecord>> =
        observeState().map { state -> state.history() }.distinctUntilChanged()

    override suspend fun restorePurchases(): PurchaseEntitlement {
        latency.await(MockOperation.RESTORE)
        return entitlement()
    }

    override suspend fun unlock(applicationId: String): CreditSpend = mutex.withLock {
        val state = load()
        val unlocked = state.copy(unlockedApplicationIds = state.unlockedApplicationIds + applicationId)
        when {
            applicationId in state.unlockedApplicationIds -> CreditSpend.Spent(state.toEntitlement(), kind = null)
            state.freeCredits > 0 -> spend(unlocked.copy(freeCredits = state.freeCredits - 1), CreditKind.FREE)
            state.purchasedCredits > 0 ->
                spend(unlocked.copy(spentPurchasedCredits = state.spentPurchasedCredits + 1), CreditKind.PURCHASED)
            else -> CreditSpend.NoCreditLeft
        }
    }

    override suspend fun clearCredits(): PurchaseEntitlement = mutex.withLock {
        save(PaymentState(freeCredits = 0, closed = true)).toEntitlement()
    }

    private suspend fun resolve(state: PaymentState, pack: ApplicationPack): PurchaseResult =
        when (scriptedOutcomes[pack.id] ?: PurchaseOutcome.Success) {
            PurchaseOutcome.Success -> {
                val saved = save(state.confirm(pack.id, newOrderId(), clock.now().toEpochMilliseconds()))
                PurchaseResult.Completed(saved.toEntitlement())
            }
            PurchaseOutcome.Pending -> {
                val saved = save(state.hold(pack.id, newOrderId(), clock.now().toEpochMilliseconds()))
                PurchaseResult.Pending(saved.toEntitlement())
            }
            PurchaseOutcome.Cancelled -> PurchaseResult.Cancelled
            PurchaseOutcome.Failed -> PurchaseResult.Failed(failureReason, state.toEntitlement())
        }

    private suspend fun spend(next: PaymentState, kind: CreditKind): CreditSpend =
        CreditSpend.Spent(save(next).toEntitlement(), kind)

    private fun newOrderId(): String = ORDER_PREFIX + idGenerator.newId()

    private fun PaymentState.history(): List<PurchaseRecord> = purchases.map { it.toModel() }.reversed()

    private fun observeState(): Flow<PaymentState> =
        store.observeValue(PAYMENT_STATE_KEY, PaymentState.serializer()).map { it ?: PaymentState(startingFreeCredits) }

    private suspend fun load(): PaymentState =
        store.readValue(PAYMENT_STATE_KEY, PaymentState.serializer()) ?: PaymentState(startingFreeCredits)

    private suspend fun save(state: PaymentState): PaymentState {
        store.writeValue(PAYMENT_STATE_KEY, PaymentState.serializer(), state)
        return state
    }

    private companion object {
        const val DEFAULT_FREE_CREDITS = 1
        const val ORDER_PREFIX = "mock-order-"
    }
}
