package com.hirehop.app.auth

import com.hirehop.app.ai.RemoteJobAnalysisSource
import com.hirehop.app.billing.RemotePaymentGateway
import com.hirehop.core.data.mock.MockStateStore
import com.hirehop.core.data.repository.PendingReportQueue
import com.hirehop.core.data.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignOutCleaner @Inject constructor(
    private val payments: RemotePaymentGateway,
    private val analysis: RemoteJobAnalysisSource,
    private val reports: PendingReportQueue,
    private val store: MockStateStore,
    private val sessionRepository: SessionRepository,
) {
    suspend fun clear() {
        payments.clearCredits()
        analysis.clear()
        reports.clear()
        store.removeWithPrefix(TAILORING_REQUEST_PREFIX)
        sessionRepository.clearConsent()
    }

    private companion object {
        const val TAILORING_REQUEST_PREFIX = "tailoring.request."
    }
}
