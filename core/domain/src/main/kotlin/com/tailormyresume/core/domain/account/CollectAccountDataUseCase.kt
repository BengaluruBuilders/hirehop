package com.tailormyresume.core.domain.account

import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.CoverLetterRepository
import com.tailormyresume.core.data.repository.ExportHistoryRepository
import com.tailormyresume.core.data.repository.PrepPlanRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.domain.PaymentGateway
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.time.Clock

class CollectAccountDataUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val applicationRepository: ApplicationRepository,
    private val exportHistoryRepository: ExportHistoryRepository,
    private val coverLetterRepository: CoverLetterRepository,
    private val prepPlanRepository: PrepPlanRepository,
    private val paymentGateway: PaymentGateway,
    private val clock: Clock,
) {
    suspend operator fun invoke(): AccountData {
        val applications = applicationRepository.observeApplications().first()
        return AccountData(
            generatedAt = clock.now(),
            account = sessionRepository.observeAccount().first(),
            consent = sessionRepository.observeConsent().first(),
            profile = profileRepository.observeProfile().first(),
            applications = applications,
            entitlement = paymentGateway.entitlement(),
            purchases = paymentGateway.purchaseHistory(),
            exports = exportHistoryRepository.observeExports().first(),
            coverLetters = applications.mapNotNull { application ->
                coverLetterRepository.observeLetter(application.id).first()?.let { application.id to it }
            }.toMap(),
            prepPlans = applications.associate { it.id to prepPlanRepository.observeItems(it.id).first() },
        )
    }
}
