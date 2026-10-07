package com.hirehop.core.domain.sample

import com.hirehop.core.data.mock.MockStateStore
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ExportHistoryRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.domain.AnalyzeJobUseCase
import com.hirehop.core.domain.CreditSpend
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.TailorResumeUseCase
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.ExportRecord
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.model.TailoredResume
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@Singleton
class OfflineSampleDataController @Inject constructor(
    private val store: MockStateStore,
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val applicationRepository: ApplicationRepository,
    private val exportHistoryRepository: ExportHistoryRepository,
    private val paymentGateway: PaymentGateway,
    private val analyzeJob: AnalyzeJobUseCase,
    private val tailorResume: TailorResumeUseCase,
    private val clock: Clock,
) : SampleDataController {

    override suspend fun load() {
        reset()
        val now = clock.now()
        sessionRepository.saveAccount(SampleDataSet.account)
        sessionRepository.recordConsent(
            ConsentRecord(
                purposes = ConsentPurpose.entries.toSet(),
                acceptedAt = now - CONSENT_DAYS_AGO.days,
                noticeVersion = ConsentRecord.CURRENT_NOTICE_VERSION,
            ),
        )
        profileRepository.saveProfile(SampleDataSet.profile)
        paymentGateway.purchase(SampleDataSet.PURCHASED_PACK_ID)
        SampleDataSet.applications.forEach { plan -> applicationRepository.upsertApplication(build(plan, now)) }
        SampleDataSet.exports.forEach { plan -> recordExport(plan, now) }
        sessionRepository.markOnboardingComplete()
    }

    override suspend fun reset() {
        sessionRepository.clear()
        exportHistoryRepository.clear()
        store.clear()
        profileRepository.clearProfile()
        applicationRepository.observeApplications().first().forEach { application ->
            applicationRepository.deleteApplication(application.id)
        }
    }

    override suspend fun keepSampleJobDescription() {
        sessionRepository.keepJobDescription(SampleDataSet.keptJob)
    }

    override suspend fun clearSampleJobDescription() {
        sessionRepository.clearKeptJobDescription()
    }

    private suspend fun build(plan: SampleApplicationPlan, now: Instant): JobApplication {
        val analysis = analyzeJob(SampleDataSet.profile, plan.jobText)
        val tailored = tailorResume(SampleDataSet.profile, analysis.job, analysis.gap)
        return JobApplication(
            id = plan.id,
            job = analysis.job,
            status = plan.status,
            notes = plan.notes,
            gapAnalysis = analysis.gap,
            tailoredResume = reviewed(tailored, plan.review),
            createdAt = now - plan.createdDaysAgo.days,
            updatedAt = now - plan.updatedDaysAgo.days,
        )
    }

    private fun reviewed(resume: TailoredResume, plan: ReviewPlan): TailoredResume {
        val reviewedCount = when (plan) {
            ReviewPlan.ALL -> resume.bullets.size
            ReviewPlan.PARTIAL -> resume.bullets.size / 2
            ReviewPlan.NONE -> 0
        }
        var changedSeen = 0
        val bullets = resume.bullets.mapIndexed { index, bullet ->
            if (index >= reviewedCount) return@mapIndexed bullet
            val changed = bullet.proposedText != bullet.originalText
            if (changed) changedSeen++
            bullet.decidedAs(if (changed && changedSeen % REJECT_EVERY == 0) BulletDecision.REJECTED else BulletDecision.ACCEPTED)
        }
        return resume.copy(bullets = bullets)
    }

    private fun TailoredBullet.decidedAs(decision: BulletDecision) = copy(decision = decision)

    private suspend fun recordExport(plan: SampleExportPlan, now: Instant) {
        val spend = paymentGateway.unlock(plan.applicationId)
        exportHistoryRepository.record(
            ExportRecord(
                applicationId = plan.applicationId,
                format = plan.format,
                fileName = plan.fileName,
                exportedAt = now - plan.exportedDaysAgo.days,
                creditKind = (spend as? CreditSpend.Spent)?.kind,
            ),
        )
    }

    private companion object {
        const val CONSENT_DAYS_AGO = 30
        const val REJECT_EVERY = 4
    }
}
