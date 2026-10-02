package com.hirehop.core.testing.gateway

import com.hirehop.core.data.mock.MockStateStore
import com.hirehop.core.data.mock.NoMockLatency
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.domain.PurchaseOutcome
import com.hirehop.core.domain.offline.OfflinePaymentGateway
import com.hirehop.core.testing.mock.TestMockStateStore
import com.hirehop.core.testing.util.TestClock
import com.hirehop.core.testing.util.TestIdGenerator

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
