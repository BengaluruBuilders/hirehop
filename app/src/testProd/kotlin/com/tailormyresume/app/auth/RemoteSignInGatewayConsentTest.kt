package com.tailormyresume.app.auth

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.app.billing.signOutCleaner
import com.tailormyresume.core.domain.onboarding.ObserveStartDestinationUseCase
import com.tailormyresume.core.domain.onboarding.StartDestination
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test
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
}
