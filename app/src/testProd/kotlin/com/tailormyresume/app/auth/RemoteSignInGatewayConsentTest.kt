package com.tailormyresume.app.auth

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.app.ai.RemoteJobAnalysisSource
import com.tailormyresume.app.billing.FakePlayBilling
import com.tailormyresume.app.billing.FakeUid
import com.tailormyresume.app.billing.RemotePaymentGateway
import com.tailormyresume.app.billing.WalletSource
import com.tailormyresume.app.billing.idleScope
import com.tailormyresume.app.billing.signOutCleaner
import com.tailormyresume.core.data.mock.MockStateStore
import com.tailormyresume.core.data.repository.PendingReportQueue
import com.tailormyresume.core.domain.onboarding.ObserveStartDestinationUseCase
import com.tailormyresume.core.domain.onboarding.StartDestination
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.testing.mock.TestMockStateStore
import com.tailormyresume.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test
import java.io.IOException
import kotlin.time.Instant

class RemoteSignInGatewayConsentTest {
    private val server = MockWebServer().apply { start() }
    private val session = TestSessionRepository()
    private val gateway = RemoteSignInGateway(
        completeConfig,
        ScriptedCredentials(),
        ScriptedFirebase(),
        server.api(),
        session,
        server.signOutCleaner(session),
    )

    @After
    fun tearDown() = runCatching { server.shutdown() }.let { }

    @Test
    fun signInAfterSignOutLeavesNoConsentSoStartIsWelcome() = runTest {
        server.enqueue(jsonResponse(200, ME_BODY))
        server.enqueue(jsonResponse(200, ME_BODY))
        gateway.signIn()
        session.markOnboardingComplete()
        session.recordConsent(ConsentRecord(setOf(ConsentPurpose.AI_PROCESSING), Instant.fromEpochMilliseconds(1), "2026-10-b"))

        gateway.signOut()
        gateway.signIn()

        assertThat(session.observeConsent().first()).isNull()
        assertThat(ObserveStartDestinationUseCase(session)().first()).isEqualTo(StartDestination.Welcome)
    }

    @Test
    fun consentIsGoneEvenWhenCleanerFailsAfterAccountRemoval() = runTest {
        val inner = TestMockStateStore()
        val failingStore = object : MockStateStore by inner {
            override suspend fun removeWithPrefix(prefix: String) = throw IOException("interrupted")
        }
        val api = server.api()
        val interruptedCleaner = SignOutCleaner(
            RemotePaymentGateway(api, WalletSource(api), FakePlayBilling(), FakeUid("uid-1"), idleScope()),
            RemoteJobAnalysisSource(api, NoMatcher),
            PendingReportQueue(failingStore),
            failingStore,
            session,
        )
        val interruptedGateway = RemoteSignInGateway(completeConfig, ScriptedCredentials(), ScriptedFirebase(), api, session, interruptedCleaner)
        server.enqueue(jsonResponse(200, ME_BODY))
        interruptedGateway.signIn()
        session.markOnboardingComplete()
        session.recordConsent(ConsentRecord(setOf(ConsentPurpose.AI_PROCESSING), Instant.fromEpochMilliseconds(1), "2026-10-b"))

        runCatching { interruptedGateway.signOut() }

        assertThat(session.observeConsent().first()).isNull()
        assertThat(ObserveStartDestinationUseCase(session)().first()).isEqualTo(StartDestination.Welcome)
    }
}
