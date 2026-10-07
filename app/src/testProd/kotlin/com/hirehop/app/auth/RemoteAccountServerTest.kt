package com.hirehop.app.auth

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.ConsentRecord
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test
import kotlin.time.Instant

class RemoteAccountServerTest {
    private val server = MockWebServer().apply { start() }

    private val record = ConsentRecord(
        purposes = ConsentPurpose.entries.toSet(),
        acceptedAt = Instant.fromEpochSeconds(1),
        noticeVersion = "2026-10-B",
    )

    @After
    fun tearDown() = runCatching { server.shutdown() }.let { }

    private fun consentResponse(purpose: String) = jsonResponse(
        200,
        """{"consent":{"purpose":"$purpose","granted":true,"policyVersion":"2026-10-b","recordedAt":"t"}}""",
    )

    @Test
    fun wireNamesFollowTheContract() {
        assertThat(ConsentPurpose.entries.map { it.wireName() }).containsExactly(
            "read-and-build",
            "keep-confirmed-facts",
            "ai-processing",
            "age-18-plus",
        )
    }

    @Test
    fun oneConsentCallPerPurposeWithTheLowerCaseVersion() = runTest {
        ConsentPurpose.entries.forEach { server.enqueue(consentResponse(it.wireName())) }

        val result = RemoteConsentUploader(server.api()).upload(record)

        assertThat(result.isSuccess).isTrue()
        val bodies = List(4) { server.takeRequest().body.readUtf8() }
        assertThat(bodies).containsExactly(
            """{"purpose":"read-and-build","granted":true,"policyVersion":"2026-10-b"}""",
            """{"purpose":"keep-confirmed-facts","granted":true,"policyVersion":"2026-10-b"}""",
            """{"purpose":"ai-processing","granted":true,"policyVersion":"2026-10-b"}""",
            """{"purpose":"age-18-plus","granted":true,"policyVersion":"2026-10-b"}""",
        ).inOrder()
    }

    @Test
    fun aFailedConsentCallStopsTheUpload() = runTest {
        server.enqueue(errorResponse(500, "INTERNAL_ERROR"))

        val result = RemoteConsentUploader(server.api()).upload(record)

        assertThat(result.isFailure).isTrue()
        assertThat(server.requestCount).isEqualTo(1)
    }

    @Test
    fun deleteCallsDeleteMe() = runTest {
        server.enqueue(jsonResponse(200, """{"deletion":{"status":"done"}}"""))

        val result = RemoteServerAccountDeleter(server.api()).delete()

        assertThat(result.isSuccess).isTrue()
        val request = server.takeRequest()
        assertThat(request.method).isEqualTo("DELETE")
        assertThat(request.path).isEqualTo("/v1/me")
    }

    @Test
    fun deleteOfADeletedAccountSucceeds() = runTest {
        server.enqueue(errorResponse(409, "ACCOUNT_DELETED"))

        assertThat(RemoteServerAccountDeleter(server.api()).delete().isSuccess).isTrue()
    }

    @Test
    fun deleteFailsOnAServerError() = runTest {
        server.enqueue(errorResponse(500, "INTERNAL_ERROR"))

        assertThat(RemoteServerAccountDeleter(server.api()).delete().isFailure).isTrue()
    }

    @Test
    fun deleteFailsWhenOffline() = runTest {
        val deleter = RemoteServerAccountDeleter(server.api())
        server.shutdown()

        assertThat(deleter.delete().isFailure).isTrue()
    }
}
