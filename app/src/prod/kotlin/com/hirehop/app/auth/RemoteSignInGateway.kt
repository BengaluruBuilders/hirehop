package com.hirehop.app.auth

import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.domain.SignInAccount
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.SignInResult
import com.hirehop.core.network.ApiError
import com.hirehop.core.network.ApiException
import com.hirehop.core.network.HirehopApi
import com.hirehop.core.network.apiResult
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteSignInGateway @Inject constructor(
    private val config: FirebaseConfig,
    private val credentials: GoogleCredentialSource,
    private val firebase: FirebaseSessionClient,
    private val api: HirehopApi,
    private val sessionRepository: SessionRepository,
) : SignInGateway {

    override suspend fun currentAccount(): SignInAccount? = sessionRepository.observeAccount().first()

    override suspend fun signIn(): SignInResult {
        if (!config.isComplete) return AuthFailure.NotConfigured.toSignInResult()
        val googleToken = when (val outcome = credentials.idToken()) {
            is AuthOutcome.Success -> outcome.value
            is AuthOutcome.Failure -> return outcome.failure.toSignInResult()
        }
        val user = when (val outcome = firebase.signIn(googleToken)) {
            is AuthOutcome.Success -> outcome.value
            is AuthOutcome.Failure -> return outcome.failure.toSignInResult()
        }
        apiResult { api.me() }.onFailure { failure ->
            withContext(NonCancellable) { firebase.signOut() }
            return failure.toAuthFailure().toSignInResult()
        }
        val account = SignInAccount(id = user.uid, displayName = user.displayName.ifBlank { user.email }, email = user.email)
        sessionRepository.saveAccount(account)
        return SignInResult.SignedIn(account)
    }

    override suspend fun signOut() {
        withContext(NonCancellable) {
            firebase.signOut()
            credentials.clearState()
            sessionRepository.signOut()
        }
    }

    private fun Throwable.toAuthFailure(): AuthFailure = when ((this as? ApiException)?.error) {
        ApiError.Offline, ApiError.Timeout -> AuthFailure.Offline
        else -> AuthFailure.Failed
    }
}
