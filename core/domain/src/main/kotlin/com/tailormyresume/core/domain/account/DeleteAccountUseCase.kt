package com.tailormyresume.core.domain.account

import com.tailormyresume.core.data.mock.MockLatency
import com.tailormyresume.core.data.mock.MockOperation
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ExportHistoryRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.domain.SignInGateway
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.ExportRecord
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.core.model.factCounts
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
    private val serverAccountDeleter: ServerAccountDeleter,
    private val creditBalance: AccountCreditBalance,
    private val latency: MockLatency,
    private val exportedFiles: ExportedFiles = ExportedFiles.None,
) {

    suspend fun preview(): AccountDeletionCounts {
        val applications = applicationRepository.observeApplications().first()
        val profile = profileRepository.observeProfile().first()
        return countsOf(applications, profile)
    }

    suspend fun refreshedPreview(): AccountDeletionCounts = preview()

    suspend operator fun invoke(
        onStep: suspend (AccountDeletionStep) -> Unit = {},
    ): AccountDeletionResult {
        val applications = applicationRepository.observeApplications().first()
        val profile = profileRepository.observeProfile().first()
        val exports = exportHistoryRepository.observeExports().first()
        val counts = countsOf(applications, profile)
        if (serverAccountDeleter.delete().isFailure) return AccountDeletionResult.Failed(dataIntact = true)
        var creditsTouched = false
        return try {
            startStep(AccountDeletionStep.DELETING_PROFILE_FACTS, onStep)
            profileRepository.clearProfile()
            exportHistoryRepository.clear()
            startStep(AccountDeletionStep.DELETING_APPLICATIONS, onStep)
            applications.forEach { application ->
                applicationRepository.deleteApplication(application.id)
            }
            startStep(AccountDeletionStep.CLOSING_ACCOUNT, onStep)
            creditsTouched = true
            creditBalance.clearUnusedCredits()
            withContext(NonCancellable) {
                signInGateway.signOut()
                sessionRepository.clear()
                exportedFiles.deleteAll()
            }
            AccountDeletionResult.Deleted(counts)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            val serverCopyGone = serverAccountDeleter.deletesRemoteData
            val restored = !serverCopyGone && restore(applications, profile, exports, onStep)
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
