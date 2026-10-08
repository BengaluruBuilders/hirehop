package com.tailormyresume.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.mock.NoMockLatency
import com.tailormyresume.core.domain.account.ExportedFiles
import com.tailormyresume.core.testing.mock.TestMockStateStore
import com.tailormyresume.core.testing.repository.TestSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OfflineSignInGatewayExportFilesTest {

    private var deleteAllCalls = 0
    private val session = TestSessionRepository()
    private val gateway = OfflineSignInGateway(session, NoMockLatency, TestMockStateStore(), ExportedFiles { deleteAllCalls++ })

    @Test
    fun signOutDeletesExportFiles() = runTest {
        gateway.signIn()

        gateway.signOut()

        assertThat(deleteAllCalls).isEqualTo(1)
    }

    @Test
    fun signInKeepsExportFiles() = runTest {
        gateway.signIn()

        assertThat(deleteAllCalls).isEqualTo(0)
    }
}
