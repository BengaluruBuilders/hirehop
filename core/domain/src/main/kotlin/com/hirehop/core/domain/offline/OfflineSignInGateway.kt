package com.hirehop.core.domain.offline

import com.hirehop.core.domain.SignInAccount
import com.hirehop.core.domain.SignInFailureReason
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.SignInOutcome
import com.hirehop.core.domain.SignInResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class OfflineSignInGateway @Inject constructor() : SignInGateway {

    private val mutex = Mutex()

    private var outcome: SignInOutcome = SignInOutcome.SignedIn
    private var account: SignInAccount = SignInAccount.localAccount
    private var signedInAccount: SignInAccount? = null

    fun withOutcome(outcome: SignInOutcome): OfflineSignInGateway = apply { this.outcome = outcome }

    fun withAccount(account: SignInAccount): OfflineSignInGateway = apply { this.account = account }

    override suspend fun currentAccount(): SignInAccount? = mutex.withLock { signedInAccount }

    override suspend fun signIn(): SignInResult = mutex.withLock {
        when (outcome) {
            SignInOutcome.SignedIn -> SignInResult.SignedIn(account).also { signedInAccount = account }
            SignInOutcome.Cancelled -> SignInResult.Cancelled
            SignInOutcome.Failed -> SignInResult.Failed(SignInFailureReason.ProviderUnavailable)
        }
    }

    override suspend fun signOut() {
        mutex.withLock { signedInAccount = null }
    }
}
