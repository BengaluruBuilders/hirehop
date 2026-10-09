package com.tailormyresume.core.model

import kotlin.time.Instant

enum class ConsentPurpose { READ_AND_BUILD, KEEP_CONFIRMED_FACTS, AI_PROCESSING, AGE_18_PLUS }

data class ConsentRecord(
    val purposes: Set<ConsentPurpose>,
    val acceptedAt: Instant,
    val noticeVersion: String,
) {
    val isCurrent: Boolean get() = noticeVersion == CURRENT_NOTICE_VERSION

    companion object {
        const val CURRENT_NOTICE_VERSION = "2026-10-b"
    }
}
