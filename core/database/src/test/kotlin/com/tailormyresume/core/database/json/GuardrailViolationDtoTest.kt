package com.tailormyresume.core.database.json

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Test

class GuardrailViolationDtoTest {

    private val allViolations: List<GuardrailViolationDto> = listOf(
        GuardrailViolationDto.MissingSource,
        GuardrailViolationDto.UnsupportedNumber("40%"),
        GuardrailViolationDto.UnsupportedTerm("Kubernetes"),
        GuardrailViolationDto.VerbEscalation(from = "assisted", to = "led"),
        GuardrailViolationDto.UnsupportedScaleClaim("millions of users"),
    )
    private val serializer = ListSerializer(GuardrailViolationDto.serializer())

    @Test
    fun everySubtypeRoundTripsAsPolymorphicList() {
        val json = TmrJson.encode(serializer, allViolations)

        assertThat(TmrJson.decode(serializer, json)).isEqualTo(allViolations)
    }

    @Test
    fun eachSubtypeWritesItsOwnDiscriminator() {
        val json = TmrJson.encode(serializer, allViolations)

        listOf(
            "missing_source",
            "unsupported_number",
            "unsupported_term",
            "verb_escalation",
            "unsupported_scale_claim",
        ).forEach { discriminator ->
            assertThat(json).contains("\"type\":\"$discriminator\"")
        }
    }

    @Test
    fun unknownKeysAreIgnoredOnDecode() {
        val json = """[{"type":"unsupported_term","term":"Rust","extra":1}]"""

        assertThat(TmrJson.decode(serializer, json))
            .containsExactly(GuardrailViolationDto.UnsupportedTerm("Rust"))
    }
}
