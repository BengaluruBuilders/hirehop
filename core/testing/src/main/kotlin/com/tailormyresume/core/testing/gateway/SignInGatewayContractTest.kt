package com.tailormyresume.core.testing.gateway

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.SignInGateway
import com.tailormyresume.core.domain.SignInResult
import kotlinx.coroutines.test.runTest
import org.junit.Test

abstract class SignInGatewayContractTest {

    protected abstract fun createSignInGateway(): SignInGateway

    @Test
    fun aSignedInAccountCarriesAnIdentity() = runTest {
        val gateway = createSignInGateway()

        val result = gateway.signIn()

        if (result is SignInResult.SignedIn) {
            assertThat(result.account.id).isNotEmpty()
            assertThat(result.account.displayName).isNotEmpty()
            assertThat(result.account.email).isNotEmpty()
        }
    }

    @Test
    fun aSignedInResultMakesThatAccountCurrent() = runTest {
        val gateway = createSignInGateway()

        val result = gateway.signIn()

        if (result is SignInResult.SignedIn) {
            assertThat(gateway.currentAccount()).isEqualTo(result.account)
        } else {
            assertThat(gateway.currentAccount()).isNull()
        }
    }

    @Test
    fun aResultThatIsNotSignedInLeavesNoCurrentAccount() = runTest {
        val gateway = createSignInGateway()

        val result = gateway.signIn()

        if (result !is SignInResult.SignedIn) {
            assertThat(gateway.currentAccount()).isNull()
        }
    }

    @Test
    fun signOutRemovesTheCurrentAccount() = runTest {
        val gateway = createSignInGateway()
        gateway.signIn()
        if (gateway.currentAccount() == null) return@runTest

        gateway.signOut()

        assertThat(gateway.currentAccount()).isNull()
    }

    @Test
    fun signOutIsSafeWhenNobodyIsSignedIn() = runTest {
        val gateway = createSignInGateway()

        gateway.signOut()

        assertThat(gateway.currentAccount()).isNull()
    }

    @Test
    fun signingInAgainReportsTheSameAccount() = runTest {
        val gateway = createSignInGateway()
        val first = gateway.signIn()

        val second = gateway.signIn()

        if (first is SignInResult.SignedIn && second is SignInResult.SignedIn) {
            assertThat(second.account).isEqualTo(first.account)
        }
    }
}
