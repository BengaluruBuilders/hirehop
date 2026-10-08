package com.tailormyresume.app.auth

import com.tailormyresume.core.common.network.di.ApplicationScope
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.network.ConsentRequiredListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentRevoker @Inject constructor(
    private val sessionRepository: SessionRepository,
    @ApplicationScope private val scope: CoroutineScope,
) : ConsentRequiredListener {
    override fun onConsentRequired() {
        scope.launch { sessionRepository.clearConsent() }
    }
}
