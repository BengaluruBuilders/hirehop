package com.hirehop.core.testing.gateway

import com.hirehop.core.data.mock.MockStateStore
import com.hirehop.core.data.mock.NoMockLatency
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.domain.SignInAccount
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.SignInOutcome
import com.hirehop.core.domain.offline.OfflineSignInGateway
import com.hirehop.core.testing.mock.TestMockStateStore
import com.hirehop.core.testing.repository.TestSessionRepository

class TestSignInGateway private constructor(
    private val delegate: OfflineSignInGateway,
    val sessionRepository: SessionRepository,
) : SignInGateway by delegate {

    constructor(
        sessionRepository: SessionRepository = TestSessionRepository(),
        store: MockStateStore = TestMockStateStore(),
    ) : this(OfflineSignInGateway(sessionRepository, NoMockLatency, store), sessionRepository)

    fun withOutcome(outcome: SignInOutcome): TestSignInGateway = apply { delegate.withOutcome(outcome) }

    fun withAccount(account: SignInAccount): TestSignInGateway = apply { delegate.withAccount(account) }
}
