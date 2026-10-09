package com.tailormyresume.app.ai

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.testing.mock.TestMockStateStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteResumeTailorRetryAfterTest {
    private val backend = FakeBackend()
    private val gap = GapAnalysis(listOf(matchOf(evidence = arrayOf(FACT_ID))), KeywordCoverage(1, 1))
    private val tailor = RemoteResumeTailor(backend.api, PendingTailoringIds(TestMockStateStore(), FixedIds))

    @After
    fun tearDown() = backend.shutdown()

    private fun plainRateLimit(retryAfter: String) {
        backend.server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", retryAfter))
        backend.reply(202, tailoringBody("SUCCEEDED", tailoringResult(FACT_TEXT)))
    }

    @Test
    fun aRetryAfterOfADayFallsBackToTheTenSecondDefault() = runTest {
        plainRateLimit("86400")

        tailor.tailor(candidate, job, gap, "app-1", null)

        assertThat(currentTime).isEqualTo(10_000L)
        assertThat(backend.server.requestCount).isEqualTo(2)
    }

    @Test
    fun aRetryAfterOfExactlyAnHourIsHonoured() = runTest {
        plainRateLimit("3600")

        tailor.tailor(candidate, job, gap, "app-1", null)

        assertThat(currentTime).isEqualTo(3_600_000L)
    }

    @Test
    fun aRetryAfterOfZeroFallsBackToTheTenSecondDefault() = runTest {
        plainRateLimit("0")

        tailor.tailor(candidate, job, gap, "app-1", null)

        assertThat(currentTime).isEqualTo(10_000L)
    }
}
