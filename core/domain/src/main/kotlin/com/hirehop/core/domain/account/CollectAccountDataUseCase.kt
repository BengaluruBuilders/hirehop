package com.hirehop.core.domain.account

import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ExportHistoryRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.domain.PaymentGateway
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.time.Clock

class CollectAccountDataUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val applicationRepository: ApplicationRepository,
    private val exportHistoryRepository: ExportHistoryRepository,
    private val paymentGateway: PaymentGateway,
    private val clock: Clock,
) {
    suspend operator fun invoke(): AccountData = AccountData(
        generatedAt = clock.now(),
        account = sessionRepository.observeAccount().first(),
        consent = sessionRepository.observeConsent().first(),
        profile = profileRepository.observeProfile().first(),
        applications = applicationRepository.observeApplications().first(),
        entitlement = paymentGateway.entitlement(),
        purchases = paymentGateway.purchaseHistory(),
        exports = exportHistoryRepository.observeExports().first(),
    )
}
