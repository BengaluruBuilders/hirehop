package com.hirehop.core.network

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.network.dto.AnalysisRequest
import com.hirehop.core.network.dto.AnalysisResponse
import com.hirehop.core.network.dto.ConsentRequest
import com.hirehop.core.network.dto.ConsentResponse
import com.hirehop.core.network.dto.ContentReportRequest
import com.hirehop.core.network.dto.ContentReportResponse
import com.hirehop.core.network.dto.CoverLetterRequest
import com.hirehop.core.network.dto.CoverLetterResponse
import com.hirehop.core.network.dto.DeletionResponse
import com.hirehop.core.network.dto.JobDto
import com.hirehop.core.network.dto.MatchDto
import com.hirehop.core.network.dto.MeResponse
import com.hirehop.core.network.dto.PacksResponse
import com.hirehop.core.network.dto.PrepQuestionsRequest
import com.hirehop.core.network.dto.PrepQuestionsResponse
import com.hirehop.core.network.dto.ProfileFactsDto
import com.hirehop.core.network.dto.PurchaseRequest
import com.hirehop.core.network.dto.PurchaseResponse
import com.hirehop.core.network.dto.PurchasesResponse
import com.hirehop.core.network.dto.ResumeParseRequest
import com.hirehop.core.network.dto.ResumeParseResponse
import com.hirehop.core.network.dto.ServerExportResponse
import com.hirehop.core.network.dto.TailoringResponse
import com.hirehop.core.network.dto.TailoringStartRequest
import com.hirehop.core.network.dto.UnlockResponse
import com.hirehop.core.network.dto.WalletResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import org.junit.Test

class DtoRoundTripTest {
    private val json = hirehopJson()
    private val plain = Json

    @Test
    fun everyContractExampleRoundTrips() {
        roundTrip<ProfileFactsDto>("profile-facts")
        roundTrip<JobDto>("job")
        roundTrip<MatchDto>("match")
        roundTrip<MeResponse>("me-response")
        roundTrip<ConsentRequest>("consent-request")
        roundTrip<ConsentResponse>("consent-response")
        roundTrip<DeletionResponse>("deletion-response")
        roundTrip<ResumeParseRequest>("resume-parse-request")
        roundTrip<ResumeParseResponse>("resume-parse-response")
        roundTrip<AnalysisRequest>("analysis-request")
        roundTrip<AnalysisResponse>("analysis-response")
        roundTrip<TailoringStartRequest>("tailoring-start-request")
        roundTrip<TailoringResponse>("tailoring-start-response")
        roundTrip<TailoringResponse>("tailoring-poll-response")
        roundTrip<PrepQuestionsRequest>("prep-questions-request")
        roundTrip<PrepQuestionsResponse>("prep-questions-response")
        roundTrip<CoverLetterRequest>("cover-letter-request")
        roundTrip<CoverLetterResponse>("cover-letter-response")
        roundTrip<WalletResponse>("wallet-response")
        roundTrip<UnlockResponse>("unlock-response")
        roundTrip<PacksResponse>("packs-response")
        roundTrip<PurchaseRequest>("purchase-request")
        roundTrip<PurchaseResponse>("purchase-response")
        roundTrip<PurchasesResponse>("purchases-response")
        roundTrip<ContentReportRequest>("content-report-request")
        roundTrip<ContentReportResponse>("content-report-response")
        roundTrip<ServerExportResponse>("me-export-response")
    }

    @Test
    fun explicitNullsAreOmittedOnTheWire() {
        val request = decode<TailoringStartRequest>("tailoring-start-request")
        assertThat(request.section).isNull()
        assertThat(json.encodeToString(TailoringStartRequest.serializer(), request)).doesNotContain("section")
    }

    private inline fun <reified T> roundTrip(name: String) {
        val text = checkNotNull(javaClass.getResource("/contract/$name.json")) { name }.readText()
        val serializer: KSerializer<T> = serializer()
        val decoded = json.decodeFromString(serializer, text)
        val encoded = json.encodeToJsonElement(serializer, decoded)
        assertThat(encoded).isEqualTo(plain.parseToJsonElement(text).withoutNulls())
        assertThat(json.decodeFromJsonElement(serializer, encoded)).isEqualTo(decoded)
    }

    private inline fun <reified T> decode(name: String): T =
        json.decodeFromString(checkNotNull(javaClass.getResource("/contract/$name.json")).readText())

    private fun JsonElement.withoutNulls(): JsonElement = when (this) {
        is JsonObject -> JsonObject(filterValues { it !is JsonNull }.mapValues { it.value.withoutNulls() })
        is JsonArray -> JsonArray(map { it.withoutNulls() })
        is JsonPrimitive -> this
    }
}
