package com.hirehop.core.data.repository

import com.hirehop.core.data.mock.MockStateStore
import com.hirehop.core.data.mock.observeValue
import com.hirehop.core.data.mock.writeValue
import com.hirehop.core.model.CareerStage
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.KeptJobDescription
import com.hirehop.core.model.SignInAccount
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

@Singleton
internal class StoredSessionRepository @Inject constructor(
    private val store: MockStateStore,
) : SessionRepository {

    override fun observeAccount(): Flow<SignInAccount?> =
        store.observeValue(ACCOUNT_KEY, AccountDto.serializer()).map { dto -> dto?.toModel() }

    override fun observeConsent(): Flow<ConsentRecord?> =
        store.observeValue(CONSENT_KEY, ConsentDto.serializer()).map { dto -> dto?.toModel() }

    override fun observeOnboardingComplete(): Flow<Boolean> =
        store.observe(ONBOARDING_KEY).map { value -> value == COMPLETE }

    override fun observeKeptJobDescription(): Flow<KeptJobDescription?> =
        store.observeValue(KEPT_JOB_KEY, KeptJobDto.serializer()).map { dto -> dto?.toModel() }

    override fun observeCareerStage(): Flow<CareerStage?> =
        store.observe(CAREER_STAGE_KEY).map { value -> CareerStage.entries.find { it.name == value } }

    override suspend fun saveCareerStage(stage: CareerStage) {
        store.write(CAREER_STAGE_KEY, stage.name)
    }

    override suspend fun saveAccount(account: SignInAccount) {
        store.writeValue(ACCOUNT_KEY, AccountDto.serializer(), AccountDto(account.id, account.displayName, account.email))
    }

    override suspend fun recordConsent(record: ConsentRecord) {
        val dto = ConsentDto(
            purposes = record.purposes.map(ConsentPurpose::name).sorted(),
            acceptedAtMillis = record.acceptedAt.toEpochMilliseconds(),
            noticeVersion = record.noticeVersion,
        )
        store.writeValue(CONSENT_KEY, ConsentDto.serializer(), dto)
    }

    override suspend fun markOnboardingComplete() {
        store.write(ONBOARDING_KEY, COMPLETE)
    }

    override suspend fun keepJobDescription(job: KeptJobDescription) {
        store.writeValue(KEPT_JOB_KEY, KeptJobDto.serializer(), KeptJobDto(job.text, job.company, job.role))
    }

    override suspend fun clearKeptJobDescription() {
        store.remove(KEPT_JOB_KEY)
    }

    override suspend fun signOut() = removeAll(ACCOUNT_KEY, KEPT_JOB_KEY)

    override suspend fun clear() = removeAll(ACCOUNT_KEY, CONSENT_KEY, ONBOARDING_KEY, KEPT_JOB_KEY, CAREER_STAGE_KEY)

    private suspend fun removeAll(vararg keys: String) = withContext(NonCancellable) {
        keys.forEach { key -> store.remove(key) }
    }

    private companion object {
        const val ACCOUNT_KEY = "session.account"
        const val CONSENT_KEY = "session.consent"
        const val ONBOARDING_KEY = "session.onboardingComplete"
        const val KEPT_JOB_KEY = "session.keptJobDescription"
        const val CAREER_STAGE_KEY = "session.careerStage"
        const val COMPLETE = "true"
    }
}

@Serializable
private data class AccountDto(val id: String, val displayName: String, val email: String) {
    fun toModel() = SignInAccount(id = id, displayName = displayName, email = email)
}

@Serializable
private data class ConsentDto(
    val purposes: List<String>,
    val acceptedAtMillis: Long,
    val noticeVersion: String,
) {
    fun toModel() = ConsentRecord(
        purposes = purposes.mapNotNull { name -> ConsentPurpose.entries.find { it.name == name } }.toSet(),
        acceptedAt = Instant.fromEpochMilliseconds(acceptedAtMillis),
        noticeVersion = noticeVersion,
    )
}

@Serializable
private data class KeptJobDto(val text: String, val company: String, val role: String) {
    fun toModel() = KeptJobDescription(text = text, company = company, role = role)
}
