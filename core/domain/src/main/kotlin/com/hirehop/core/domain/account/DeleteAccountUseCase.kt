package com.hirehop.core.domain.account

import com.hirehop.core.data.mock.MockLatency
import com.hirehop.core.data.mock.MockOperation
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ExportHistoryRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.ExportRecord
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.factCounts
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val exportHistoryRepository: ExportHistoryRepository,
    private val sessionRepository: SessionRepository,
    private val signInGateway: SignInGateway,
    private val creditBalance: AccountCreditBalance,
    private val latency: MockLatency,
) {

    suspend fun preview(): AccountDeletionCounts {
        val applications = applicationRepository.observeApplications().first()
        val profile = profileRepository.observeProfile().first()
        return countsOf(applications, profile)
    }

    suspend operator fun invoke(
        onStep: suspend (AccountDeletionStep) -> Unit = {},
    ): AccountDeletionResult {
        val applications = applicationRepository.observeApplications().first()
        val profile = profileRepository.observeProfile().first()
        val exports = exportHistoryRepository.observeExports().first()
        val counts = countsOf(applications, profile)
        var creditsTouched = false
        return try {
            startStep(AccountDeletionStep.DELETING_APPLICATIONS, onStep)
            applications.forEach { application ->
                applicationRepository.deleteApplication(application.id)
            }
            startStep(AccountDeletionStep.DELETING_PROFILE_FACTS, onStep)
            profileRepository.clearProfile()
            exportHistoryRepository.clear()
            startStep(AccountDeletionStep.CLOSING_ACCOUNT, onStep)
            creditsTouched = true
            creditBalance.clearUnusedCredits()
            withContext(NonCancellable) {
                signInGateway.signOut()
                sessionRepository.clear()
            }
            AccountDeletionResult.Deleted(counts)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            val restored = restore(applications, profile, exports, onStep)
            AccountDeletionResult.Failed(dataIntact = restored && !creditsTouched)
        }
    }

    private suspend fun startStep(step: AccountDeletionStep, onStep: suspend (AccountDeletionStep) -> Unit) {
        onStep(step)
        latency.await(MockOperation.DELETE_ACCOUNT_STEP)
    }

    private suspend fun countsOf(
        applications: List<JobApplication>,
        profile: CandidateProfile?,
    ) = AccountDeletionCounts(
        profileFacts = profile?.factCounts()?.total ?: 0,
        applications = applications.size,
        unusedCredits = creditBalance.unusedCredits(),
    )

    private suspend fun restore(
        applications: List<JobApplication>,
        profile: CandidateProfile?,
        exports: List<ExportRecord>,
        onStep: suspend (AccountDeletionStep) -> Unit,
    ): Boolean = try {
        onStep(AccountDeletionStep.DELETING_APPLICATIONS)
        applications.forEach { application ->
            applicationRepository.upsertApplication(application)
        }
        if (profile != null) {
            profileRepository.saveProfile(profile)
        }
        exportHistoryRepository.clear()
        exports.forEach { record -> exportHistoryRepository.record(record) }
        true
    } catch (failure: Exception) {
        false
    }
}
