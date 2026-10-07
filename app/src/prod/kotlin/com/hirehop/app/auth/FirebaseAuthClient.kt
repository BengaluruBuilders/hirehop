package com.hirehop.app.auth

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.hirehop.core.common.network.Dispatcher
import com.hirehop.core.common.network.HhDispatchers
import com.hirehop.core.domain.FirebaseUidProvider
import com.hirehop.core.network.IdTokenProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirebaseAuthClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val config: FirebaseConfig,
    @Dispatcher(HhDispatchers.IO) private val io: CoroutineDispatcher,
) : FirebaseSessionClient, IdTokenProvider, FirebaseUidProvider {

    private val auth: FirebaseAuth? by lazy { if (config.isComplete) createAuth() else null }

    override suspend fun signIn(googleIdToken: String): AuthOutcome<FirebaseUser> = withContext(io) {
        val firebaseAuth = auth ?: return@withContext AuthOutcome.Failure(AuthFailure.NotConfigured)
        try {
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val user = firebaseAuth.signInWithCredential(credential).await().user
                ?: return@withContext AuthOutcome.Failure(AuthFailure.Failed)
            AuthOutcome.Success(FirebaseUser(user.uid, user.displayName.orEmpty(), user.email.orEmpty()))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            AuthOutcome.Failure(failure.toAuthFailure())
        }
    }

    override fun signOut() {
        auth?.signOut()
    }

    override fun idToken(forceRefresh: Boolean): String? {
        val user = auth?.currentUser ?: return null
        return try {
            Tasks.await(user.getIdToken(forceRefresh), TOKEN_TIMEOUT_SECONDS, TimeUnit.SECONDS).token
        } catch (failure: Exception) {
            null
        }
    }

    override fun uid(): String? = auth?.currentUser?.uid

    private fun createAuth(): FirebaseAuth {
        val app = FirebaseApp.getApps(context).firstOrNull() ?: FirebaseApp.initializeApp(
            context,
            FirebaseOptions.Builder()
                .setApiKey(config.apiKey)
                .setApplicationId(config.appId)
                .setProjectId(config.projectId)
                .build(),
        )
        return FirebaseAuth.getInstance(app)
    }
}

internal fun Exception.toAuthFailure(): AuthFailure = when (this) {
    is FirebaseNetworkException -> AuthFailure.Offline
    else -> AuthFailure.Failed
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
}

private const val TOKEN_TIMEOUT_SECONDS = 10L
