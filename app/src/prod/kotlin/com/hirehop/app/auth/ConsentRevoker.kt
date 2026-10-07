package com.hirehop.app.auth

import com.hirehop.core.common.network.di.ApplicationScope
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.network.ConsentRequiredListener
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
