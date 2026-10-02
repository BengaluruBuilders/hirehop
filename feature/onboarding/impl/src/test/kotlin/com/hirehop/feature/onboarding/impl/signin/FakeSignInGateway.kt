package com.hirehop.feature.onboarding.impl.signin

import com.hirehop.core.domain.SignInAccount
import com.hirehop.core.domain.SignInFailureReason
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.SignInOutcome
import com.hirehop.core.domain.SignInResult
import com.hirehop.core.testing.gateway.TestSignInGateway

class FakeSignInGateway(
    private val result: SignInResult = SignInResult.SignedIn(SignInAccount.localAccount),
    private val current: SignInAccount? = null,
) : SignInGateway {

    var signInCount: Int = 0
        private set

    var signOutCount: Int = 0
        private set

    override suspend fun currentAccount(): SignInAccount? = current

    override suspend fun signIn(): SignInResult {
        signInCount += 1
        return result
    }

    override suspend fun signOut() {
        signOutCount += 1
    }
}

class RecoveringSignInGateway(
    private val first: SignInResult,
    private val then: SignInResult,
) : SignInGateway {

    private var callCount = 0

    var signInCount: Int = 0
        private set

    override suspend fun currentAccount(): SignInAccount? = null

    override suspend fun signIn(): SignInResult {
        signInCount += 1
        val answer = if (callCount == 0) first else then
        callCount += 1
        return answer
    }

    override suspend fun signOut() {
        callCount = 0
    }
}

fun offlineGatewayWith(outcome: SignInOutcome): TestSignInGateway = TestSignInGateway().withOutcome(outcome)

fun failureWith(reason: SignInFailureReason): SignInResult = SignInResult.Failed(reason)
