package com.hirehop.core.domain.gateway

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.mock.NoMockLatency
import com.hirehop.core.domain.SignInAccount
import com.hirehop.core.domain.SignInFailureReason
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.SignInOutcome
import com.hirehop.core.domain.SignInResult
import com.hirehop.core.domain.offline.OfflineSignInGateway
import com.hirehop.core.testing.gateway.SignInGatewayContractTest
import com.hirehop.core.testing.mock.TestMockStateStore
import com.hirehop.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OfflineSignInGatewayContractTest : SignInGatewayContractTest() {

    override fun createSignInGateway(): SignInGateway = offlineGateway()

    @Test
    fun theDefaultOutcomeSignsInTheLocalAccount() = runTest {
        val gateway = offlineGateway()

        val result = gateway.signIn()

        assertThat(result).isEqualTo(SignInResult.SignedIn(SignInAccount.localAccount))
    }

    @Test
    fun theCancelledOutcomeLeavesNobodySignedIn() = runTest {
        val gateway = offlineGateway().withOutcome(SignInOutcome.Cancelled)

        val result = gateway.signIn()

        assertThat(result).isEqualTo(SignInResult.Cancelled)
        assertThat(gateway.currentAccount()).isNull()
    }

    @Test
    fun theFailedOutcomeReportsTheProviderAndLeavesNobodySignedIn() = runTest {
        val gateway = offlineGateway().withOutcome(SignInOutcome.Failed)

        val result = gateway.signIn()

        assertThat(result).isEqualTo(SignInResult.Failed(SignInFailureReason.ProviderUnavailable))
        assertThat(gateway.currentAccount()).isNull()
    }

    @Test
    fun theSameInputAlwaysProducesTheSameResult() = runTest {
        val first = offlineGateway().withOutcome(SignInOutcome.SignedIn)
        val second = offlineGateway().withOutcome(SignInOutcome.SignedIn)

        assertThat(second.signIn()).isEqualTo(first.signIn())
    }

    @Test
    fun aCancelledAttemptAfterASignInLeavesTheEarlierAccountInPlace() = runTest {
        val gateway = offlineGateway()
        gateway.signIn()

        val result = offlineGateway().withOutcome(SignInOutcome.Cancelled).signIn()

        assertThat(result).isEqualTo(SignInResult.Cancelled)
        assertThat(gateway.currentAccount()).isEqualTo(SignInAccount.localAccount)
    }

    @Test
    fun aChosenAccountIsTheAccountThatSignsIn() = runTest {
        val account = SignInAccount(id = "local.account.2", displayName = "Asha Rao", email = "asha.rao@example.com")
        val gateway = offlineGateway().withAccount(account)

        gateway.signIn()

        assertThat(gateway.currentAccount()).isEqualTo(account)
    }

    private fun offlineGateway(): OfflineSignInGateway = OfflineSignInGateway(TestSessionRepository(), NoMockLatency, TestMockStateStore())
}
