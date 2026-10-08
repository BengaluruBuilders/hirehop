package com.tailormyresume.core.testing.gateway

import com.tailormyresume.core.data.mock.MockStateStore
import com.tailormyresume.core.data.mock.NoMockLatency
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.domain.PurchaseFailureReason
import com.tailormyresume.core.domain.PurchaseOutcome
import com.tailormyresume.core.domain.offline.OfflinePaymentGateway
import com.tailormyresume.core.testing.mock.TestMockStateStore
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.core.testing.util.TestIdGenerator

class TestPaymentGateway private constructor(
    private val delegate: OfflinePaymentGateway,
) : PaymentGateway by delegate {

    constructor(
        store: MockStateStore = TestMockStateStore(),
        clock: TestClock = TestClock(),
    ) : this(OfflinePaymentGateway(store, NoMockLatency, clock, TestIdGenerator("order")))

    fun withOutcome(packId: String, outcome: PurchaseOutcome): TestPaymentGateway =
        apply { delegate.withOutcome(packId, outcome) }

    fun withFailureReason(reason: PurchaseFailureReason): TestPaymentGateway =
        apply { delegate.withFailureReason(reason) }

    fun withFreeCredits(credits: Int): TestPaymentGateway = apply { delegate.withFreeCredits(credits) }
}
