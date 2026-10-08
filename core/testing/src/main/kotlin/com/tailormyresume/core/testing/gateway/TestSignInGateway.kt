package com.tailormyresume.core.testing.gateway

import com.tailormyresume.core.data.mock.MockStateStore
import com.tailormyresume.core.data.mock.NoMockLatency
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.domain.SignInAccount
import com.tailormyresume.core.domain.SignInGateway
import com.tailormyresume.core.domain.SignInOutcome
import com.tailormyresume.core.domain.offline.OfflineSignInGateway
import com.tailormyresume.core.testing.mock.TestMockStateStore
import com.tailormyresume.core.testing.repository.TestSessionRepository

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
