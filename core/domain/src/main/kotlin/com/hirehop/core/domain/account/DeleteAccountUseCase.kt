package com.hirehop.core.domain.account

import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.JobApplication
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val creditBalance: AccountCreditBalance,
) {

    suspend fun preview(): AccountDeletionCounts {
        val applications = applicationRepository.observeApplications().first()
        val profile = profileRepository.observeProfile().first()
        return AccountDeletionCounts(
            profileFacts = profile?.entries?.size ?: 0,
            applications = applications.size,
            unusedCredits = creditBalance.unusedCredits(),
        )
    }

    suspend operator fun invoke(
        onStep: suspend (AccountDeletionStep) -> Unit = {},
    ): AccountDeletionResult {
        val applications = applicationRepository.observeApplications().first()
        val profile = profileRepository.observeProfile().first()
        val counts = AccountDeletionCounts(
            profileFacts = profile?.entries?.size ?: 0,
            applications = applications.size,
            unusedCredits = creditBalance.unusedCredits(),
        )
        var creditsTouched = false
        return try {
            onStep(AccountDeletionStep.DELETING_APPLICATIONS)
            applications.forEach { application ->
                applicationRepository.deleteApplication(application.id)
            }
            onStep(AccountDeletionStep.DELETING_PROFILE_FACTS)
            profileRepository.clearProfile()
            onStep(AccountDeletionStep.CLOSING_ACCOUNT)
            creditsTouched = true
            creditBalance.clearUnusedCredits()
            AccountDeletionResult.Deleted(counts)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            val restored = restore(applications = applications, profile = profile, onStep = onStep)
            AccountDeletionResult.Failed(dataIntact = restored && !creditsTouched)
        }
    }

    private suspend fun restore(
        applications: List<JobApplication>,
        profile: CandidateProfile?,
        onStep: suspend (AccountDeletionStep) -> Unit,
    ): Boolean = try {
        onStep(AccountDeletionStep.DELETING_APPLICATIONS)
        applications.forEach { application ->
            applicationRepository.upsertApplication(application)
        }
        if (profile != null) {
            profileRepository.saveProfile(profile)
        }
        true
    } catch (failure: Exception) {
        false
    }
}
