package com.tailormyresume.core.data.repository

import com.tailormyresume.core.model.CareerStage
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.KeptJobDescription
import com.tailormyresume.core.model.SignInAccount
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeAccount(): Flow<SignInAccount?>

    fun observeConsent(): Flow<ConsentRecord?>

    fun observeOnboardingComplete(): Flow<Boolean>

    fun observeKeptJobDescription(): Flow<KeptJobDescription?>

    fun observeCareerStage(): Flow<CareerStage?>

    suspend fun saveCareerStage(stage: CareerStage)

    suspend fun clearCareerStage()

    suspend fun saveAccount(account: SignInAccount)

    suspend fun lastAccountId(): String?

    suspend fun saveLastAccountId(id: String)

    suspend fun recordConsent(record: ConsentRecord)

    suspend fun markOnboardingComplete()

    suspend fun keepJobDescription(job: KeptJobDescription)

    suspend fun clearKeptJobDescription()

    suspend fun clearConsent()

    suspend fun signOut()

    suspend fun clear()
}
