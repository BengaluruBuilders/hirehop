package com.tailormyresume.feature.analysis.impl.result

internal data class MustHaveRow(
    val requirementId: String,
    val text: String,
    val reason: String?,
    val unclear: Boolean,
)

internal sealed interface JobResultUiState {
    data object Loading : JobResultUiState

    data object Unavailable : JobResultUiState

    data class Ready(
        val title: String,
        val company: String,
        val location: String,
        val now: Int,
        val upTo: Int,
        val have: List<String>,
        val missing: List<String>,
        val mustHaves: List<MustHaveRow>,
        val credits: Int,
        val asksQuestion: Boolean,
    ) : JobResultUiState {
        val keywordCount: Int get() = have.size + missing.size

        val subtitle: String get() = listOf(company, location).filter { it.isNotBlank() }.joinToString(" · ")
    }
}

internal sealed interface JobResultEvent {
    data class Paywall(val applicationId: String) : JobResultEvent

    data class QuickQuestion(val applicationId: String) : JobResultEvent

    data class Tailor(val applicationId: String) : JobResultEvent
}
