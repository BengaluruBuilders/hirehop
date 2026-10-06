package com.hirehop.core.testing.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.model.CareerStage
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.KeptJobDescription
import com.hirehop.core.model.SignInAccount
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

abstract class SessionRepositoryContractTest {

    protected abstract fun createSessionRepository(): SessionRepository

    private val consent = ConsentRecord(
        purposes = setOf(ConsentPurpose.READ_AND_BUILD, ConsentPurpose.KEEP_CONFIRMED_FACTS),
        acceptedAt = Instant.fromEpochMilliseconds(1_790_000_000_000),
        noticeVersion = ConsentRecord.CURRENT_NOTICE_VERSION,
    )
    private val keptJob = KeptJobDescription(text = "Analyst role\nSQL", company = "Northwind GCC", role = "Analyst")

    @Test
    fun aNewSessionIsEmpty() = runTest {
        val session = createSessionRepository()

        assertThat(session.observeAccount().first()).isNull()
        assertThat(session.observeConsent().first()).isNull()
        assertThat(session.observeOnboardingComplete().first()).isFalse()
        assertThat(session.observeKeptJobDescription().first()).isNull()
    }

    @Test
    fun aSavedAccountIsObserved() = runTest {
        val session = createSessionRepository()

        session.saveAccount(SignInAccount.localAccount)

        assertThat(session.observeAccount().first()).isEqualTo(SignInAccount.localAccount)
    }

    @Test
    fun aRecordedConsentKeepsPurposesTimeAndNoticeVersion() = runTest {
        val session = createSessionRepository()

        session.recordConsent(consent)

        assertThat(session.observeConsent().first()).isEqualTo(consent)
    }

    @Test
    fun aSavedCareerStageIsObservedAndClearedWithTheSession() = runTest {
        val session = createSessionRepository()
        assertThat(session.observeCareerStage().first()).isNull()

        session.saveCareerStage(CareerStage.JUST_STARTING_OUT)
        assertThat(session.observeCareerStage().first()).isEqualTo(CareerStage.JUST_STARTING_OUT)

        session.clear()
        assertThat(session.observeCareerStage().first()).isNull()
    }

    @Test
    fun markingOnboardingCompleteIsObserved() = runTest {
        val session = createSessionRepository()

        session.observeOnboardingComplete().test {
            assertThat(awaitItem()).isFalse()
            session.markOnboardingComplete()
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun aKeptJobDescriptionKeepsTextCompanyAndRole() = runTest {
        val session = createSessionRepository()

        session.keepJobDescription(keptJob)

        assertThat(session.observeKeptJobDescription().first()).isEqualTo(keptJob)
    }

    @Test
    fun clearingTheKeptJobDescriptionLeavesTheRestOfTheSession() = runTest {
        val session = createSessionRepository()
        session.saveAccount(SignInAccount.localAccount)
        session.keepJobDescription(keptJob)

        session.clearKeptJobDescription()

        assertThat(session.observeKeptJobDescription().first()).isNull()
        assertThat(session.observeAccount().first()).isEqualTo(SignInAccount.localAccount)
    }

    @Test
    fun clearRemovesEveryPartOfTheSession() = runTest {
        val session = createSessionRepository()
        session.saveAccount(SignInAccount.localAccount)
        session.recordConsent(consent)
        session.markOnboardingComplete()
        session.keepJobDescription(keptJob)

        session.clear()

        assertThat(session.observeAccount().first()).isNull()
        assertThat(session.observeConsent().first()).isNull()
        assertThat(session.observeOnboardingComplete().first()).isFalse()
        assertThat(session.observeKeptJobDescription().first()).isNull()
    }

    @Test
    fun signOutRemovesTheAccountAndTheKeptJobDescriptionOnly() = runTest {
        val session = createSessionRepository()
        session.saveAccount(SignInAccount.localAccount)
        session.recordConsent(consent)
        session.markOnboardingComplete()
        session.keepJobDescription(keptJob)

        session.signOut()

        assertThat(session.observeAccount().first()).isNull()
        assertThat(session.observeKeptJobDescription().first()).isNull()
        assertThat(session.observeConsent().first()).isEqualTo(consent)
        assertThat(session.observeOnboardingComplete().first()).isTrue()
    }

    @Test
    fun clearOnAnEmptySessionIsHarmless() = runTest {
        val session = createSessionRepository()

        session.clear()

        assertThat(session.observeAccount().first()).isNull()
    }
}
