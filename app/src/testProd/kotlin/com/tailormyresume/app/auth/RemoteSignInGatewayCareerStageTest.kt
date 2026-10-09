package com.tailormyresume.app.auth

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.app.billing.signOutCleaner
import com.tailormyresume.core.model.CareerStage
import com.tailormyresume.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test

class RemoteSignInGatewayCareerStageTest {
    private val server = MockWebServer().apply { start() }
    private val firebase = ScriptedFirebase()
    private val session = TestSessionRepository()
    private val gateway = RemoteSignInGateway(
        completeConfig,
        ScriptedCredentials(),
        firebase,
        server.api(),
        session,
        server.signOutCleaner(session),
        LocalDataWiper { session.clear() },
    )

    @After
    fun tearDown() = runCatching { server.shutdown() }.let { }

    @Test
    fun aNewAccountDoesNotInheritThePreviousAccountsCareerStage() = runTest {
        server.enqueue(jsonResponse(200, ME_BODY))
        server.enqueue(jsonResponse(200, ME_BODY))
        gateway.signIn()
        session.saveCareerStage(CareerStage.ONE_TO_TWO_YEARS_IN)
        gateway.signOut()
        firebase.outcome = AuthOutcome.Success(FirebaseUser("uid-2", "Ravi", "ravi@example.com"))

        gateway.signIn()

        assertThat(session.observeAccount().first()?.id).isEqualTo("uid-2")
        assertThat(session.observeCareerStage().first()).isNull()
    }

    @Test
    fun aStageChosenBeforeSigningInSurvivesTheAccountSwitchWipe() = runTest {
        server.enqueue(jsonResponse(200, ME_BODY))
        session.saveLastAccountId("uid-0")
        session.saveCareerStage(CareerStage.JUST_STARTING_OUT)

        gateway.signIn()

        assertThat(session.observeCareerStage().first()).isEqualTo(CareerStage.JUST_STARTING_OUT)
    }
}
