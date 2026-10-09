package com.tailormyresume.app.ai

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test

class RemoteCoverLetterEvidenceTest {
    private val backend = FakeBackend()

    @After
    fun tearDown() = backend.shutdown()

    @Test
    fun theRemoteSourceChoosesItsOwnEvidenceSoTheAppShowsNoFactCount() {
        assertThat(RemoteCoverLetterSource(backend.api).choosesEvidence).isFalse()
    }
}
