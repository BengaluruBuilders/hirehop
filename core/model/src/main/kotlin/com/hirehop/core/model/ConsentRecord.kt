package com.hirehop.core.model

import kotlin.time.Instant

enum class ConsentPurpose { READ_AND_BUILD, ANALYSE_ON_DEVICE, KEEP_CONFIRMED_FACTS }

data class ConsentRecord(
    val purposes: Set<ConsentPurpose>,
    val acceptedAt: Instant,
    val noticeVersion: String,
) {
    companion object {
        const val CURRENT_NOTICE_VERSION = "2026-10-b"
    }
}
