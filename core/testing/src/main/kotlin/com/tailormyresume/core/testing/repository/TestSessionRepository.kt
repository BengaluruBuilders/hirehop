package com.tailormyresume.core.testing.repository

import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.model.CareerStage
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.KeptJobDescription
import com.tailormyresume.core.model.SignInAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestSessionRepository : SessionRepository {

    private val account = MutableStateFlow<SignInAccount?>(null)
    private val consent = MutableStateFlow<ConsentRecord?>(null)
    private val onboardingComplete = MutableStateFlow(false)
    private val keptJob = MutableStateFlow<KeptJobDescription?>(null)
    private val careerStage = MutableStateFlow<CareerStage?>(null)
    private var lastAccountId: String? = null

    override fun observeAccount(): Flow<SignInAccount?> = account

    override fun observeConsent(): Flow<ConsentRecord?> = consent

    override fun observeOnboardingComplete(): Flow<Boolean> = onboardingComplete

    override fun observeKeptJobDescription(): Flow<KeptJobDescription?> = keptJob

    override fun observeCareerStage(): Flow<CareerStage?> = careerStage

    override suspend fun saveCareerStage(stage: CareerStage) {
        careerStage.value = stage
    }

    override suspend fun clearCareerStage() {
        careerStage.value = null
    }

    override suspend fun saveAccount(account: SignInAccount) {
        this.account.value = account
    }

    override suspend fun lastAccountId(): String? = lastAccountId

    override suspend fun saveLastAccountId(id: String) {
        lastAccountId = id
    }

    override suspend fun recordConsent(record: ConsentRecord) {
        consent.value = record
    }

    override suspend fun markOnboardingComplete() {
        onboardingComplete.value = true
    }

    override suspend fun keepJobDescription(job: KeptJobDescription) {
        keptJob.value = job
    }

    override suspend fun clearKeptJobDescription() {
        keptJob.value = null
    }

    override suspend fun clearConsent() {
        consent.value = null
    }

    override suspend fun signOut() {
        account.value = null
        keptJob.value = null
    }

    override suspend fun clear() {
        account.value = null
        lastAccountId = null
        consent.value = null
        onboardingComplete.value = false
        keptJob.value = null
        careerStage.value = null
    }

    fun sendAccount(account: SignInAccount?) {
        this.account.value = account
    }

    fun sendConsent(record: ConsentRecord?) {
        consent.value = record
    }

    fun sendOnboardingComplete(complete: Boolean) {
        onboardingComplete.value = complete
    }
}
