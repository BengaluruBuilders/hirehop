package com.tailormyresume.app.auth

import com.tailormyresume.app.AppStartTask
import com.tailormyresume.core.common.network.di.ApplicationScope
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.domain.FirebaseUidProvider
import com.tailormyresume.core.domain.SignInGateway
import com.tailormyresume.core.network.SessionExpiredListener
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionExpiryHandler @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val gateway: Lazy<SignInGateway>,
    private val uidProvider: FirebaseUidProvider,
    private val config: FirebaseConfig,
    @ApplicationScope private val scope: CoroutineScope,
) : SessionExpiredListener, AppStartTask {
    private val signingOut = AtomicBoolean(false)

    override fun onSessionExpired() {
        if (!signingOut.compareAndSet(false, true)) return
        scope.launch {
            try {
                if (sessionRepository.observeAccount().first() != null) gateway.get().signOut()
            } finally {
                signingOut.set(false)
            }
        }
    }

    override fun start() {
        if (!config.isComplete) return
        scope.launch {
            if (sessionRepository.observeAccount().first() != null && uidProvider.uid() == null) {
                onSessionExpired()
            }
        }
    }
}
