package com.tailormyresume.core.database.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.time.Instant

class InstantConverterTest {

    private val converter = InstantConverter()

    @Test
    fun instantRoundTripsAtMillisecondPrecision() {
        val instant = Instant.fromEpochMilliseconds(1_700_000_123_456)

        assertThat(converter.longToInstant(converter.instantToLong(instant))).isEqualTo(instant)
    }

    @Test
    fun instantIsStoredAsEpochMilliseconds() {
        assertThat(converter.instantToLong(Instant.fromEpochMilliseconds(42))).isEqualTo(42L)
    }

    @Test
    fun nullMapsToNull() {
        assertThat(converter.instantToLong(null)).isNull()
        assertThat(converter.longToInstant(null)).isNull()
    }
}
