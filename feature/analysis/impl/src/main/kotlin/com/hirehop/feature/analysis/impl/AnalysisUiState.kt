package com.hirehop.feature.analysis.impl

import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementPriority

sealed interface AnalysisUiState {
    data object Loading : AnalysisUiState

    data object NoProfile : AnalysisUiState

    data class Input(
        val jobText: String,
        val canAnalyze: Boolean,
    ) : AnalysisUiState

    data object Analyzing : AnalysisUiState

    data class Result(
        val title: String,
        val company: String,
        val keywordCoverage: KeywordCoverage,
        val sections: List<RequirementSection>,
        val prepPlanCount: Int,
        val canSave: Boolean,
    ) : AnalysisUiState

    data object Saving : AnalysisUiState

    data class Saved(val applicationId: String) : AnalysisUiState
}

enum class RequirementGroup {
    MustHaveGaps,
    Partial,
    Met,
    NiceToHaveGaps,
}

data class RequirementSection(
    val group: RequirementGroup,
    val items: List<RequirementItem>,
)

data class RequirementItem(
    val requirement: JobRequirement,
    val status: MatchStatus,
    val evidence: List<String>,
    val isInPrepPlan: Boolean,
) {
    val id: String get() = requirement.id
    val isGap: Boolean get() = status == MatchStatus.GAP
    val isMustHave: Boolean get() = requirement.priority == RequirementPriority.MUST_HAVE
}

const val MAX_JOB_TEXT_LENGTH = 20_000
const val MIN_JOB_TEXT_LENGTH = 40
