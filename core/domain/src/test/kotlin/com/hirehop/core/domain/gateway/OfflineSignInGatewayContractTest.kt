package com.hirehop.core.domain.gateway

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.SignInAccount
import com.hirehop.core.domain.SignInFailureReason
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.SignInOutcome
import com.hirehop.core.domain.SignInResult
import com.hirehop.core.domain.offline.OfflineSignInGateway
import com.hirehop.core.testing.gateway.SignInGatewayContractTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OfflineSignInGatewayContractTest : SignInGatewayContractTest() {

    override fun createSignInGateway(): SignInGateway = OfflineSignInGateway()

    @Test
    fun theDefaultOutcomeSignsInTheLocalAccount() = runTest {
        val gateway = OfflineSignInGateway()

        val result = gateway.signIn()

        assertThat(result).isEqualTo(SignInResult.SignedIn(SignInAccount.localAccount))
    }

    @Test
    fun theCancelledOutcomeLeavesNobodySignedIn() = runTest {
        val gateway = OfflineSignInGateway().withOutcome(SignInOutcome.Cancelled)

        val result = gateway.signIn()

        assertThat(result).isEqualTo(SignInResult.Cancelled)
        assertThat(gateway.currentAccount()).isNull()
    }

    @Test
    fun theFailedOutcomeReportsTheProviderAndLeavesNobodySignedIn() = runTest {
        val gateway = OfflineSignInGateway().withOutcome(SignInOutcome.Failed)

        val result = gateway.signIn()

        assertThat(result).isEqualTo(SignInResult.Failed(SignInFailureReason.ProviderUnavailable))
        assertThat(gateway.currentAccount()).isNull()
    }

    @Test
    fun theSameInputAlwaysProducesTheSameResult() = runTest {
        val first = OfflineSignInGateway().withOutcome(SignInOutcome.SignedIn)
        val second = OfflineSignInGateway().withOutcome(SignInOutcome.SignedIn)

        assertThat(second.signIn()).isEqualTo(first.signIn())
    }

    @Test
    fun aCancelledAttemptAfterASignInLeavesTheEarlierAccountInPlace() = runTest {
        val gateway = OfflineSignInGateway()
        gateway.signIn()

        val result = OfflineSignInGateway().withOutcome(SignInOutcome.Cancelled).signIn()

        assertThat(result).isEqualTo(SignInResult.Cancelled)
        assertThat(gateway.currentAccount()).isEqualTo(SignInAccount.localAccount)
    }

    @Test
    fun aChosenAccountIsTheAccountThatSignsIn() = runTest {
        val account = SignInAccount(id = "local.account.2", displayName = "Asha Rao", email = "asha.rao@example.com")
        val gateway = OfflineSignInGateway().withAccount(account)

        gateway.signIn()

        assertThat(gateway.currentAccount()).isEqualTo(account)
    }
}
