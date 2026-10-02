package com.hirehop.core.data.repository

import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.KeptJobDescription
import com.hirehop.core.model.SignInAccount
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeAccount(): Flow<SignInAccount?>

    fun observeConsent(): Flow<ConsentRecord?>

    fun observeOnboardingComplete(): Flow<Boolean>

    fun observeKeptJobDescription(): Flow<KeptJobDescription?>

    suspend fun saveAccount(account: SignInAccount)

    suspend fun recordConsent(record: ConsentRecord)

    suspend fun markOnboardingComplete()

    suspend fun keepJobDescription(job: KeptJobDescription)

    suspend fun clearKeptJobDescription()

    suspend fun signOut()

    suspend fun clear()
}
