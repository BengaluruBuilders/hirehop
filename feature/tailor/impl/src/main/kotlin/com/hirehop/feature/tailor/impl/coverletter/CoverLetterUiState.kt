package com.hirehop.feature.tailor.impl.coverletter

import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.coverletter.CoverLetterComposer
import com.hirehop.core.domain.coverletter.CoverLetterDraft
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario

enum class CoverLetterStage {
    GENERATING,
    READY,
    NO_MATCHING_EVIDENCE,
    EMPTY_PROFILE,
    OFFLINE,
    ERROR,
}

enum class CoverLetterBasis {
    PLAIN,
    JOB_DESCRIPTION,
    PROFILE_NAME,
    CONFIRMED_FACT,
    NO_MATCHING_EVIDENCE,
}

enum class CoverLetterMessage {
    COPIED,
    SAVED,
    REPORT_UNAVAILABLE,
}

data class CoverLetterFactRef(
    val factId: String,
    val entryTitle: String,
    val text: String,
)

data class CoverLetterSentence(
    val text: String,
    val factId: String?,
)

data class CoverLetterParagraph(
    val ordinal: Int,
    val text: String,
    val sentences: List<CoverLetterSentence>,
    val facts: List<CoverLetterFactRef>,
    val basis: CoverLetterBasis,
    val isUserEdited: Boolean = false,
)

data class CoverLetterUiState(
    val stage: CoverLetterStage = CoverLetterStage.GENERATING,
    val jobTitle: String = "",
    val jobCompany: String = "",
    val paragraphs: List<CoverLetterParagraph> = emptyList(),
    val isOffline: Boolean = false,
    val editingOrdinal: Int? = null,
    val editingText: String = "",
    val copiedText: String = "",
    val message: CoverLetterMessage? = null,
) {
    val wordCount: Int get() = paragraphs.sumOf { paragraph -> paragraph.text.wordCount() }

    val letterText: String get() = paragraphs.joinToString(separator = "\n\n") { paragraph -> paragraph.text }

    val editedParagraph: CoverLetterParagraph?
        get() = paragraphs.firstOrNull { paragraph -> paragraph.ordinal == editingOrdinal }

    val isEditing: Boolean get() = editedParagraph != null
}

data class CoverLetterInputs(
    val profile: CandidateProfile?,
    val analysis: JobAnalysisResult,
    val draft: CoverLetterDraft,
    val isOffline: Boolean,
)

fun coverLetterStageFor(scenario: DebugScenario): CoverLetterStage = when (scenario) {
    DebugScenario.LOADING -> CoverLetterStage.GENERATING
    DebugScenario.ERROR -> CoverLetterStage.ERROR
    else -> CoverLetterStage.GENERATING
}

fun coverLetterIsStatic(scenario: DebugScenario): Boolean =
    scenario == DebugScenario.LOADING || scenario == DebugScenario.ERROR

fun coverLetterIsOffline(scenario: DebugScenario): Boolean = scenario == DebugScenario.OFFLINE

fun coverLetterStateFor(inputs: CoverLetterInputs): CoverLetterUiState {
    val analysis = inputs.analysis
    val facts = confirmedFactsOf(inputs.profile)
    if (facts.isEmpty()) {
        return CoverLetterUiState(
            stage = CoverLetterStage.EMPTY_PROFILE,
            jobTitle = analysis.job.title,
            jobCompany = analysis.job.company,
            isOffline = inputs.isOffline,
        )
    }
    val context = CoverLetterBasisContext(
        fullName = inputs.profile?.fullName.orEmpty(),
        jobTitle = analysis.job.title,
        jobCompany = analysis.job.company,
        facts = facts,
    )
    val paragraphs = listOf(
        inputs.draft.greeting,
        inputs.draft.openingParagraph,
        inputs.draft.evidenceParagraph,
        inputs.draft.closingParagraph,
    )
        .filter { part -> part.isNotBlank() }
        .mapIndexed { index, part -> part.toParagraph(ordinal = index + 1, context = context) }
    val hasQuotedFact = paragraphs.any { paragraph -> paragraph.basis == CoverLetterBasis.CONFIRMED_FACT }
    return CoverLetterUiState(
        stage = when {
            inputs.isOffline -> CoverLetterStage.OFFLINE
            hasQuotedFact -> CoverLetterStage.READY
            else -> CoverLetterStage.NO_MATCHING_EVIDENCE
        },
        jobTitle = analysis.job.title,
        jobCompany = analysis.job.company,
        paragraphs = paragraphs,
        isOffline = inputs.isOffline,
    )
}

private data class CoverLetterBasisContext(
    val fullName: String,
    val jobTitle: String,
    val jobCompany: String,
    val facts: List<CoverLetterFactRef>,
)

private val SENTENCE_BREAK = Regex("(?<=[.!?])\\s+")

private val WHITESPACE = Regex("\\s+")

private const val HH_TARGET_MIN_WORDS = 150

private const val HH_TARGET_MAX_WORDS = 220

internal fun String.wordCount(): Int = trim()
    .split(WHITESPACE)
    .count { token -> token.isNotEmpty() }

internal fun isWithinWordTarget(count: Int): Boolean = count in HH_TARGET_MIN_WORDS..HH_TARGET_MAX_WORDS

internal fun String.sentences(): List<String> = if (isBlank()) {
    emptyList()
} else {
    split(SENTENCE_BREAK)
        .map { sentence -> sentence.trim() }
        .filter { sentence -> sentence.isNotEmpty() }
}

internal fun confirmedFactsOf(profile: CandidateProfile?): List<CoverLetterFactRef> {
    if (profile == null) return emptyList()
    return profile.entries
        .filter { entry -> entry.isConfirmed }
        .flatMap { entry ->
            entry.bullets.map { bullet ->
                CoverLetterFactRef(
                    factId = bullet.id,
                    entryTitle = entry.title.trim(),
                    text = bullet.text,
                )
            }
        }
        .filter { fact -> fact.text.isNotBlank() }
        .distinctBy { fact -> fact.factId }
}

private fun String.toParagraph(
    ordinal: Int,
    context: CoverLetterBasisContext,
): CoverLetterParagraph {
    val sentences = sentences().map { sentence -> sentence.toSentence(context.facts) }
    val cited = sentences.mapNotNull { sentence -> sentence.factId }
        .distinct()
        .mapNotNull { factId -> context.facts.firstOrNull { fact -> fact.factId == factId } }
    return CoverLetterParagraph(
        ordinal = ordinal,
        text = trim(),
        sentences = sentences,
        facts = cited,
        basis = basisOf(text = trim(), cited = cited, context = context),
    )
}

private fun String.toSentence(facts: List<CoverLetterFactRef>): CoverLetterSentence {
    val probe = normalise()
    val match = facts.firstOrNull { fact -> fact.quotesIn(probe) }
    return CoverLetterSentence(text = trim(), factId = match?.factId)
}

private fun CoverLetterFactRef.quotesIn(probe: String): Boolean {
    val quoted = text.normalise()
    return quoted.isNotEmpty() && probe.contains(quoted)
}

private fun String.normalise(): String = lowercase()
    .replace('’', '\'')
    .replace(WHITESPACE, " ")
    .trim()

private fun basisOf(
    text: String,
    cited: List<CoverLetterFactRef>,
    context: CoverLetterBasisContext,
): CoverLetterBasis = when {
    text == CoverLetterComposer.NO_MATCHING_EVIDENCE -> CoverLetterBasis.NO_MATCHING_EVIDENCE
    cited.isNotEmpty() -> CoverLetterBasis.CONFIRMED_FACT
    context.fullName.isNotBlank() && text.contains(context.fullName.trim()) -> CoverLetterBasis.PROFILE_NAME
    context.jobTitle.isNotBlank() && text.contains(context.jobTitle.trim()) -> CoverLetterBasis.JOB_DESCRIPTION
    context.jobCompany.isNotBlank() && text.contains(context.jobCompany.trim()) -> CoverLetterBasis.JOB_DESCRIPTION
    else -> CoverLetterBasis.PLAIN
}
