package com.hirehop.feature.tailor.impl.prepquestions

import com.hirehop.core.domain.prep.PrepQuestion
import com.hirehop.core.domain.prep.PrepQuestionGenerator
import com.hirehop.core.domain.prep.PrepQuestionKind
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario

enum class PrepQuestionsStage {
    GENERATING,
    READY,
    EMPTY_ANALYSIS,
    EMPTY_PROFILE,
    OFFLINE,
    ERROR,
}

enum class PrepQuestionFilter(val kind: PrepQuestionKind?) {
    ALL(null),
    STRENGTH(PrepQuestionKind.STRENGTH),
    CLARIFY(PrepQuestionKind.CLARIFY),
    GAP(PrepQuestionKind.GAP),
}

enum class PrepQuestionsMessage {
    PRACTISED,
    UNPRACTISED,
    REPORT_UNAVAILABLE,
}

data class PrepQuestionCard(
    val id: String,
    val kind: PrepQuestionKind,
    val ordinal: Int,
    val prompt: String,
    val requirementText: String,
    val backingFactId: String?,
    val isPractised: Boolean = false,
)

data class PrepQuestionGroup(
    val kind: PrepQuestionKind,
    val cards: List<PrepQuestionCard>,
)

data class PrepQuestionsUiState(
    val stage: PrepQuestionsStage = PrepQuestionsStage.GENERATING,
    val jobTitle: String = "",
    val jobCompany: String = "",
    val groups: List<PrepQuestionGroup> = emptyList(),
    val filter: PrepQuestionFilter = PrepQuestionFilter.ALL,
    val isOffline: Boolean = false,
    val message: PrepQuestionsMessage? = null,
) {
    val totalCount: Int get() = groups.sumOf { group -> group.cards.size }

    val practisedCount: Int get() = groups.sumOf { group -> group.cards.count { card -> card.isPractised } }

    val visibleGroups: List<PrepQuestionGroup>
        get() = if (filter.kind == null) {
            groups
        } else {
            groups.filter { group -> group.kind == filter.kind }
        }

    fun countOf(filter: PrepQuestionFilter): Int {
        if (filter.kind == null) return totalCount
        return groups.firstOrNull { group -> group.kind == filter.kind }?.cards?.size ?: 0
    }

    fun cardOf(id: String): PrepQuestionCard? = groups
        .flatMap { group -> group.cards }
        .firstOrNull { card -> card.id == id }
}

data class PrepQuestionsInputs(
    val profile: CandidateProfile?,
    val questions: List<PrepQuestion>,
    val jobTitle: String,
    val jobCompany: String,
    val isOffline: Boolean,
)

fun prepQuestionsStageFor(scenario: DebugScenario): PrepQuestionsStage = when (scenario) {
    DebugScenario.LOADING -> PrepQuestionsStage.GENERATING
    DebugScenario.ERROR -> PrepQuestionsStage.ERROR
    else -> PrepQuestionsStage.GENERATING
}

fun prepQuestionsIsStatic(scenario: DebugScenario): Boolean =
    scenario == DebugScenario.LOADING || scenario == DebugScenario.ERROR

fun prepQuestionsIsOffline(scenario: DebugScenario): Boolean = scenario == DebugScenario.OFFLINE

fun prepQuestionsFilterFor(scenario: DebugScenario): PrepQuestionFilter = when (scenario) {
    DebugScenario.PARTIAL -> PrepQuestionFilter.GAP
    else -> PrepQuestionFilter.ALL
}

fun prepQuestionsStateFor(inputs: PrepQuestionsInputs): PrepQuestionsUiState {
    val header = PrepQuestionsUiState(
        jobTitle = inputs.jobTitle,
        jobCompany = inputs.jobCompany,
        isOffline = inputs.isOffline,
    )
    if (inputs.profile == null || !inputs.profile.hasConfirmedFact()) {
        return header.copy(stage = PrepQuestionsStage.EMPTY_PROFILE)
    }
    val questions = inputs.questions.take(PrepQuestionGenerator.MAX_QUESTIONS)
    if (questions.isEmpty()) {
        return header.copy(stage = PrepQuestionsStage.EMPTY_ANALYSIS)
    }
    val groups = GROUP_ORDER.map { kind -> questions.toGroup(kind = kind) }
        .filter { group -> group.cards.isNotEmpty() }
    return header.copy(
        stage = if (inputs.isOffline) PrepQuestionsStage.OFFLINE else PrepQuestionsStage.READY,
        groups = groups,
    )
}

val GROUP_ORDER: List<PrepQuestionKind> = listOf(
    PrepQuestionKind.STRENGTH,
    PrepQuestionKind.CLARIFY,
    PrepQuestionKind.GAP,
)

private fun CandidateProfile.hasConfirmedFact(): Boolean = entries
    .any { entry -> entry.isConfirmed && entry.bullets.any { bullet -> bullet.text.isNotBlank() } }

private fun List<PrepQuestion>.toGroup(kind: PrepQuestionKind): PrepQuestionGroup {
    val ofKind = filter { question -> question.kind == kind }
    return PrepQuestionGroup(
        kind = kind,
        cards = ofKind.mapIndexed { index, question -> question.toCard(ordinal = index + 1) },
    )
}

private fun PrepQuestion.toCard(ordinal: Int): PrepQuestionCard = PrepQuestionCard(
    id = id,
    kind = kind,
    ordinal = ordinal,
    prompt = prompt,
    requirementText = requirementText,
    backingFactId = backingFactId,
)
