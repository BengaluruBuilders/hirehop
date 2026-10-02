package com.hirehop.core.model

data class TailoringReviewState(
    val regenerationsBySection: Map<String, Int> = emptyMap(),
    val editedBulletIds: Set<String> = emptySet(),
) {
    val regenerationsUsed: Int get() = regenerationsBySection.values.sum()

    fun regenerationsUsedIn(section: String): Int = regenerationsBySection[section] ?: 0
}
