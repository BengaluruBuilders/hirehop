package com.tailormyresume.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.KeptJobDescription
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.testing.mock.TestMockStateStore
import com.tailormyresume.core.testing.repository.SessionRepositoryContractTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class StoredSessionRepositoryTest : SessionRepositoryContractTest() {

    override fun createSessionRepository(): SessionRepository = StoredSessionRepository(TestMockStateStore())

    @Test
    fun theWholeSessionSurvivesARestartOfTheRepository() = runTest {
        val store = TestMockStateStore()
        val consent = ConsentRecord(
            purposes = setOf(ConsentPurpose.AI_PROCESSING),
            acceptedAt = Instant.fromEpochMilliseconds(1_790_000_123_000),
            noticeVersion = "2026-10",
        )
        val job = KeptJobDescription(text = "line one\nline two", company = "Northwind GCC", role = "Analyst")
        StoredSessionRepository(store).apply {
            saveAccount(SignInAccount.localAccount)
            recordConsent(consent)
            markOnboardingComplete()
            keepJobDescription(job)
        }

        val restarted = StoredSessionRepository(store)

        assertThat(restarted.observeAccount().first()).isEqualTo(SignInAccount.localAccount)
        assertThat(restarted.observeConsent().first()).isEqualTo(consent)
        assertThat(restarted.observeOnboardingComplete().first()).isTrue()
        assertThat(restarted.observeKeptJobDescription().first()).isEqualTo(job)
    }

    @Test
    fun anUnknownConsentPurposeInStoredStateIsDropped() = runTest {
        val store = TestMockStateStore()
        store.write("session.consent", """{"purposes":["READ_AND_BUILD","FUTURE_PURPOSE"],"acceptedAtMillis":1,"noticeVersion":"1"}""")

        val consent = StoredSessionRepository(store).observeConsent().first()

        assertThat(consent?.purposes).containsExactly(ConsentPurpose.READ_AND_BUILD)
    }
}
