package com.tailormyresume.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.KeptJobDescription
import com.tailormyresume.core.testing.mock.TestMockStateStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StoredSessionRepositoryKeptFlagsTest {

    @Test
    fun keptJob_keepsThePrefillFlags() = runTest {
        val store = TestMockStateStore()
        val job = KeptJobDescription("jd", company = "Co", role = "Role", companyIsPrefill = true, roleIsPrefill = true)
        StoredSessionRepository(store).keepJobDescription(job)

        val restored = StoredSessionRepository(store).observeKeptJobDescription().first()

        assertThat(restored).isEqualTo(job)
    }

    @Test
    fun keptJobWithoutFlags_readsAsTyped() = runTest {
        val store = TestMockStateStore()
        store.write("session.keptJobDescription", """{"text":"jd","company":"Co","role":"Role"}""")

        val restored = StoredSessionRepository(store).observeKeptJobDescription().first()

        assertThat(restored?.roleIsPrefill).isFalse()
        assertThat(restored?.companyIsPrefill).isFalse()
    }
}
