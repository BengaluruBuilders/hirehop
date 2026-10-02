package com.hirehop.core.testing.util

import com.hirehop.core.domain.IdGenerator
import kotlin.time.Clock
import kotlin.time.Instant

class TestClock(var instant: Instant = Instant.fromEpochSeconds(1_790_000_000)) : Clock {
    override fun now(): Instant = instant
}

class TestIdGenerator(private val prefix: String = "id") : IdGenerator {
    private var counter = 0

    override fun newId(): String = "$prefix-${++counter}"
}
