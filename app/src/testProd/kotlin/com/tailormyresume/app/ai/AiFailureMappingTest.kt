package com.tailormyresume.app.ai

import com.google.common.truth.Truth.assertWithMessage
import com.tailormyresume.core.domain.AiException
import com.tailormyresume.core.domain.AiFailure
import com.tailormyresume.core.domain.GapMatcher
import com.tailormyresume.core.domain.ImportRemovalNotice
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.testing.mock.TestMockStateStore
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Test

class AiFailureMappingTest {
    private val backend = FakeBackend()
    private val analysis = JobAnalysisResult(job, GapAnalysis(listOf(matchOf(evidence = arrayOf(FACT_ID))), KeywordCoverage(1, 1)))

    @After
    fun tearDown() = backend.shutdown()

    private fun routes(): Map<String, suspend () -> Any> {
        val matcher = object : GapMatcher {
            override fun match(profile: com.tailormyresume.core.model.CandidateProfile, job: com.tailormyresume.core.model.JobDescription) =
                analysis.gap
        }
        val tailor = RemoteResumeTailor(backend.api, PendingTailoringIds(TestMockStateStore(), FixedIds))
        return mapOf(
            "parse" to { RemoteResumeTextParser(backend.api, FactIdAllocator(), ImportRemovalNotice()).parse("x".repeat(60)) },
            "analyse" to { RemoteJobAnalysisSource(backend.api, matcher).analyse(candidate, "jd text") },
            "tailor" to { tailor.tailor(candidate, job, analysis.gap, "app-1", null) },
            "prep" to { RemotePrepQuestionSource(backend.api)(analysis, candidate, 6) },
            "letter" to { RemoteCoverLetterSource(backend.api)(candidate, job, analysis, 3) },
        )
    }

    private fun failureOf(route: suspend () -> Any): AiFailure? =
        runBlocking { runCatching { route() }.exceptionOrNull() }.let { (it as? AiException)?.failure }

    @Test
    fun everyRouteMapsEveryErrorCode() {
        val expected = mapOf(
            (403 to "CONSENT_REQUIRED") to AiFailure.ConsentRequired,
            (402 to "NO_CREDIT") to AiFailure.NoCredit,
            (429 to "ALLOWANCE_EXHAUSTED") to AiFailure.AllowanceExhausted,
            (400 to "INVALID_INPUT") to AiFailure.InvalidInput,
            (413 to "PAYLOAD_TOO_LARGE") to AiFailure.InvalidInput,
            (409 to "ACCOUNT_DELETED") to AiFailure.AccountDeleted,
            (409 to "ANALYSIS_IN_PROGRESS") to AiFailure.Unavailable,
            (502 to "AI_PROVIDER_ERROR") to AiFailure.Unavailable,
            (500 to "INTERNAL_ERROR") to AiFailure.Unavailable,
            (429 to "QUOTA_EXCEEDED") to AiFailure.Unavailable,
            (404 to "NOT_FOUND") to AiFailure.Unavailable,
        )
        routes().forEach { (name, route) ->
            expected.forEach { (response, failure) ->
                backend.fail(response.first, response.second)
                assertWithMessage("$name ${response.second}").that(failureOf(route)).isEqualTo(failure)
            }
        }
    }

    @Test
    fun unreachableServerIsNetwork() {
        val routes = routes()
        backend.shutdown()
        routes.forEach { (name, route) -> assertWithMessage(name).that(failureOf(route)).isEqualTo(AiFailure.Network) }
    }

    @Test
    fun silentServerIsTimeout() {
        routes().filterKeys { it != "tailor" }.forEach { (name, route) ->
            backend.server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            assertWithMessage(name).that(failureOf(route)).isEqualTo(AiFailure.Timeout)
        }
    }

    @Test
    fun malformedBodyIsUnavailable() {
        routes().forEach { (name, route) ->
            backend.reply(200, "{not json")
            assertWithMessage(name).that(failureOf(route)).isEqualTo(AiFailure.Unavailable)
        }
    }
}
