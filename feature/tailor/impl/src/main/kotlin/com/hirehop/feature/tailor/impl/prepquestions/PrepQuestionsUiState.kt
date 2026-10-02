package com.hirehop.feature.tailor.impl.prepquestions

import com.hirehop.core.domain.prep.PrepQuestion
import com.hirehop.core.domain.prep.PrepQuestionGenerator
import com.hirehop.core.domain.prep.PrepQuestionKind
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario
import com.hirehop.feature.tailor.impl.coverletter.CoverLetterFactRef
import com.hirehop.feature.tailor.impl.coverletter.confirmedFactsOf

enum class PrepQuestionsStage {
    GENERATING,
    READY,
    EMPTY_ANALYSIS,
    EMPTY_PROFILE,
    ERROR,
}

enum class PrepQuestionsMessage {
    REPORTED,
}

data class PrepQuestionCard(
    val id: String,
    val kind: PrepQuestionKind,
    val ordinal: Int,
    val prompt: String,
    val requirementText: String,
    val fact: CoverLetterFactRef?,
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
    val isOffline: Boolean = false,
    val message: PrepQuestionsMessage? = null,
    val reportedIds: Set<String> = emptySet(),
) {
    val factCards: List<PrepQuestionCard>
        get() = groups.filter { it.kind != PrepQuestionKind.GAP }.flatMap { it.cards }

    val gapCards: List<PrepQuestionCard>
        get() = groups.filter { it.kind == PrepQuestionKind.GAP }.flatMap { it.cards }

    val questionCount: Int get() = factCards.size

    val totalCount: Int get() = groups.sumOf { it.cards.size }

    fun cardOf(id: String): PrepQuestionCard? = groups
        .flatMap { group -> group.cards }
        .firstOrNull { card -> card.id == id }
}

data class PrepQuestionsInputs(
    val profile: CandidateProfile?,
    val questions: List<PrepQuestion>,
    val jobTitle: String,
    val jobCompany: String,
)

fun prepQuestionsStageFor(scenario: DebugScenario): PrepQuestionsStage = when (scenario) {
    DebugScenario.ERROR -> PrepQuestionsStage.ERROR
    else -> PrepQuestionsStage.GENERATING
}

fun prepQuestionsIsStatic(scenario: DebugScenario): Boolean =
    scenario == DebugScenario.LOADING || scenario == DebugScenario.ERROR

fun prepQuestionsStateFor(inputs: PrepQuestionsInputs): PrepQuestionsUiState {
    val header = PrepQuestionsUiState(
        jobTitle = inputs.jobTitle,
        jobCompany = inputs.jobCompany,
    )
    if (inputs.profile == null || !inputs.profile.hasConfirmedFact()) {
        return header.copy(stage = PrepQuestionsStage.EMPTY_PROFILE)
    }
    val questions = inputs.questions.take(PrepQuestionGenerator.MAX_QUESTIONS)
    if (questions.isEmpty()) {
        return header.copy(stage = PrepQuestionsStage.EMPTY_ANALYSIS)
    }
    val facts = confirmedFactsOf(inputs.profile).associateBy { it.factId }
    var ordinal = 0
    val groups = GROUP_ORDER.map { kind ->
        PrepQuestionGroup(
            kind = kind,
            cards = questions.filter { it.kind == kind }.map { question ->
                ordinal += 1
                question.toCard(ordinal = ordinal, fact = question.backingFactId?.let { facts[it] })
            },
        )
    }.filter { group -> group.cards.isNotEmpty() }
    return header.copy(stage = PrepQuestionsStage.READY, groups = groups)
}

val GROUP_ORDER: List<PrepQuestionKind> = listOf(
    PrepQuestionKind.STRENGTH,
    PrepQuestionKind.CLARIFY,
    PrepQuestionKind.GAP,
)

private fun CandidateProfile.hasConfirmedFact(): Boolean = entries
    .any { entry -> entry.isConfirmed && entry.bullets.any { bullet -> bullet.text.isNotBlank() } }

private fun PrepQuestion.toCard(ordinal: Int, fact: CoverLetterFactRef?): PrepQuestionCard = PrepQuestionCard(
    id = id,
    kind = kind,
    ordinal = ordinal,
    prompt = prompt,
    requirementText = requirementText,
    fact = fact,
)
