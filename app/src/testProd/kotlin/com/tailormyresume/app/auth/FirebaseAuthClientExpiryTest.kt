package com.tailormyresume.app.auth

import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

@RunWith(RobolectricTestRunner::class)
class FirebaseAuthClientExpiryTest {
    @Test
    fun invalidUserAndInvalidCredentialsAreSessionExpiry() {
        val disabled = ExecutionException(FirebaseAuthInvalidUserException("ERROR_USER_DISABLED", "disabled"))
        val revoked = ExecutionException(FirebaseAuthInvalidCredentialsException("ERROR_INVALID_CREDENTIAL", "revoked"))

        assertThat(disabled.isSessionExpiry()).isTrue()
        assertThat(revoked.isSessionExpiry()).isTrue()
    }

    @Test
    fun timeoutAndNetworkFailuresAreNotSessionExpiry() {
        assertThat(TimeoutException().isSessionExpiry()).isFalse()
        assertThat(ExecutionException(FirebaseNetworkException("offline")).isSessionExpiry()).isFalse()
        assertThat(ExecutionException(RuntimeException("other")).isSessionExpiry()).isFalse()
        assertThat(InterruptedException().isSessionExpiry()).isFalse()
    }
}
