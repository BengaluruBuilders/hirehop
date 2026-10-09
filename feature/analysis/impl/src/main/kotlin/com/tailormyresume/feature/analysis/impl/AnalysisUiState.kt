package com.tailormyresume.feature.analysis.impl

import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementPriority
import kotlin.time.Instant

data class JobLabel(
    val title: String = "",
    val company: String = "",
)

sealed interface AnalysisUiState {
    val job: JobLabel

    data object Loading : AnalysisUiState {
        override val job: JobLabel = JobLabel()
    }

    data class Analyzing(
        override val job: JobLabel,
        val factCount: Int,
        val keyTermCount: Int? = null,
        val requirementCount: Int? = null,
    ) : AnalysisUiState {
        val stepIndex: Int get() = if (keyTermCount == null) 0 else 1
    }

    data class Failed(override val job: JobLabel, val cause: FailureCause = FailureCause.Generic) : AnalysisUiState

    data class DailyLimit(override val job: JobLabel) : AnalysisUiState

    data class Result(
        override val job: JobLabel,
        val keywordCoverage: KeywordCoverage,
        val sections: List<RequirementSection>,
        val totalCredits: Int,
        val isOffline: Boolean = false,
        val tailorLimitReached: Boolean = false,
        val isTailoring: Boolean = false,
        val overlay: AnalysisOverlay = AnalysisOverlay.None,
        val toast: AnalysisToast? = null,
        val closedRequirementId: String? = null,
        val nextFactId: String = FIRST_USER_STATED_FACT_ID,
        val analysedAt: Instant? = null,
    ) : AnalysisUiState {
        val items: List<RequirementItem> get() = sections.flatMap { it.items }
        val gapCount: Int get() = items.count { it.isGap }
        val hasKeyTerms: Boolean get() = keywordCoverage.total > 0
        val canTailor: Boolean get() = hasKeyTerms && !isOffline && !tailorLimitReached && !isTailoring

        fun itemOrNull(requirementId: String): RequirementItem? = items.firstOrNull { it.id == requirementId }
    }
}

sealed interface FailureCause {
    data object Generic : FailureCause

    data object InProgress : FailureCause

    data class RateLimited(val retryAfterSeconds: Int?) : FailureCause

    data object QuotaReached : FailureCause

    data object SignInRequired : FailureCause
}

sealed interface AnalysisOverlay {
    data object None : AnalysisOverlay

    data class Menu(val requirementId: String) : AnalysisOverlay

    data class Source(val requirementId: String) : AnalysisOverlay

    data class Question(val requirementId: String, val notClosed: Boolean = false) : AnalysisOverlay

    data object ShareCard : AnalysisOverlay
}

sealed interface AnalysisToast {
    data class PrepAdded(val requirementId: String, val requirementText: String) : AnalysisToast

    data object GapClosed : AnalysisToast

    data object Reported : AnalysisToast

    data object EvidenceFailed : AnalysisToast

    data object TailorFailed : AnalysisToast

    data class TailorBlocked(val cause: FailureCause) : AnalysisToast

    val hasUndo: Boolean get() = this is PrepAdded || this is GapClosed
}

sealed interface AnalysisDestination {
    data class Leave(val step: OnboardingStep) : AnalysisDestination

    data class Tailor(val applicationId: String) : AnalysisDestination
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
    val skills: List<String>,
    val isInPrepPlan: Boolean,
    val isReported: Boolean = false,
    val factRefs: List<RequirementFactRef> = emptyList(),
    val userStatedSkills: List<String> = emptyList(),
) {
    val id: String get() = requirement.id
    val isGap: Boolean get() = status == MatchStatus.GAP
    val isMustHave: Boolean get() = requirement.priority == RequirementPriority.MUST_HAVE
    val hasSource: Boolean get() = factRefs.isNotEmpty() || skills.isNotEmpty()
    val hasUserStatedFact: Boolean get() = factRefs.any { it.source == FactSource.USER_STATED }
    val hasMenu: Boolean get() = !isReported || hasSource
}

data class RequirementFactRef(
    val factId: String,
    val displayId: String,
    val title: String,
    val organization: String,
    val startDate: String,
    val endDate: String,
    val lines: List<String>,
    val source: FactSource,
    val isConfirmed: Boolean,
)

const val GAP_NOTE_THRESHOLD = 5

const val FIRST_USER_STATED_FACT_ID = "U-01"
