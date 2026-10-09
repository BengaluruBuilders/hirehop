package com.tailormyresume.feature.tailor.impl.coverletter

import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.coverletter.CoverLetterComposer
import com.tailormyresume.core.domain.coverletter.CoverLetterDraft
import com.tailormyresume.core.domain.fact.FactDisplayIds
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.WrittenCoverLetter
import com.tailormyresume.core.model.WrittenParagraph
import com.tailormyresume.core.model.confirmedWithinLimits
import com.tailormyresume.core.model.evidenceIds
import kotlin.time.Instant

enum class CoverLetterStage {
    OFFER,
    GENERATING,
    READY,
    NO_MATCHING_EVIDENCE,
    EMPTY_PROFILE,
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
    SAVED,
    REPORTED,
}

data class CoverLetterFlag(
    val quote: String,
    val factId: String,
)

data class CoverLetterFactRef(
    val factId: String,
    val displayId: String,
    val entryId: String,
    val category: EntryCategory,
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
    val isGreeting: Boolean = false,
    val flag: CoverLetterFlag? = null,
)

data class CoverLetterUiState(
    val stage: CoverLetterStage = CoverLetterStage.OFFER,
    val jobTitle: String = "",
    val jobCompany: String = "",
    val exportedFileName: String? = null,
    val paragraphs: List<CoverLetterParagraph> = emptyList(),
    val isOffline: Boolean = false,
    val editingOrdinal: Int? = null,
    val editingText: String = "",
    val message: CoverLetterMessage? = null,
    val reviewedCount: Int = 0,
    val totalCount: Int = 0,
    val paragraphCount: Int = 0,
    val factCount: Int = 0,
    val showsFactCount: Boolean = true,
    val reportedIds: Set<String> = emptySet(),
    val generationId: String? = null,
    val citedFactIds: List<String>? = null,
) {
    val wordCount: Int get() = paragraphs.sumOf { paragraph -> paragraph.text.wordCount() }

    val letterText: String get() = paragraphs.joinToString(separator = "\n\n") { paragraph -> paragraph.text }

    val editedParagraph: CoverLetterParagraph?
        get() = paragraphs.firstOrNull { paragraph -> paragraph.ordinal == editingOrdinal }

    val isEditing: Boolean get() = editedParagraph != null

    val showsOfflineBanner: Boolean
        get() = isOffline && (stage == CoverLetterStage.READY || stage == CoverLetterStage.NO_MATCHING_EVIDENCE)
}

data class CoverLetterInputs(
    val profile: CandidateProfile?,
    val analysis: JobAnalysisResult,
    val draft: CoverLetterDraft,
)

fun coverLetterStageFor(scenario: DebugScenario): CoverLetterStage = when (scenario) {
    DebugScenario.LOADING -> CoverLetterStage.GENERATING
    DebugScenario.ERROR -> CoverLetterStage.ERROR
    else -> CoverLetterStage.OFFER
}

fun coverLetterIsStatic(scenario: DebugScenario): Boolean =
    scenario == DebugScenario.LOADING || scenario == DebugScenario.ERROR

fun coverLetterStateFor(inputs: CoverLetterInputs): CoverLetterUiState {
    val analysis = inputs.analysis
    val facts = confirmedFactsOf(inputs.profile)
    if (facts.isEmpty()) {
        return CoverLetterUiState(
            stage = CoverLetterStage.EMPTY_PROFILE,
            jobTitle = analysis.job.title,
            jobCompany = analysis.job.company,
        )
    }
    val context = CoverLetterBasisContext(
        fullName = inputs.profile?.fullName.orEmpty(),
        jobTitle = analysis.job.title,
        jobCompany = analysis.job.company,
        facts = facts,
    )
    val hasGreeting = inputs.draft.greeting.isNotBlank()
    val paragraphs = listOf(
        inputs.draft.greeting,
        inputs.draft.openingParagraph,
        inputs.draft.evidenceParagraph,
        inputs.draft.closingParagraph,
    )
        .filter { part -> part.isNotBlank() }
        .mapIndexed { index, part ->
            part.toParagraph(ordinal = index + 1, context = context).copy(isGreeting = hasGreeting && index == 0)
        }
    val hasQuotedFact = paragraphs.any { paragraph -> paragraph.basis == CoverLetterBasis.CONFIRMED_FACT }
    return CoverLetterUiState(
        stage = if (hasQuotedFact || inputs.draft.citesOnlyConfirmedEvidence(inputs.profile)) CoverLetterStage.READY else CoverLetterStage.NO_MATCHING_EVIDENCE,
        jobTitle = analysis.job.title,
        jobCompany = analysis.job.company,
        paragraphs = paragraphs,
        paragraphCount = paragraphs.count { !it.isGreeting },
        factCount = paragraphs.flatMap { paragraph -> paragraph.facts.map { it.displayId } }.distinct().size,
        generationId = inputs.draft.generationId,
        citedFactIds = inputs.draft.citedFactIds,
    )
}

private fun CoverLetterDraft.citesOnlyConfirmedEvidence(profile: CandidateProfile?): Boolean {
    val evidenceIds = profile?.confirmedWithinLimits()?.evidenceIds().orEmpty()
    val cited = citedFactIds.orEmpty()
    return cited.isNotEmpty() && evidenceIds.containsAll(cited)
}

fun CoverLetterUiState.toWrittenLetter(writtenAt: Instant): WrittenCoverLetter = WrittenCoverLetter(
    paragraphs = paragraphs.map { paragraph ->
        WrittenParagraph(text = paragraph.text, isGreeting = paragraph.isGreeting, isUserEdited = paragraph.isUserEdited)
    },
    writtenAt = writtenAt,
    generationId = generationId,
    citedFactIds = citedFactIds,
)

fun restoredCoverLetterState(
    profile: CandidateProfile?,
    analysis: JobAnalysisResult,
    written: WrittenCoverLetter,
): CoverLetterUiState? {
    val body = written.paragraphs.filterNot { it.isGreeting }.map { it.text }
    val draft = CoverLetterDraft(
        greeting = written.paragraphs.firstOrNull { it.isGreeting }?.text.orEmpty(),
        openingParagraph = body.getOrElse(0) { "" },
        evidenceParagraph = body.getOrElse(1) { "" },
        closingParagraph = body.getOrElse(2) { "" },
        generationId = written.generationId,
        citedFactIds = written.citedFactIds,
    )
    val state = coverLetterStateFor(CoverLetterInputs(profile = profile, analysis = analysis, draft = draft))
    if (state.paragraphs.isEmpty()) return null
    val editedOrdinals = written.paragraphs.mapIndexedNotNull { index, paragraph ->
        (index + 1).takeIf { paragraph.isUserEdited }
    }.toSet()
    return state.copy(
        stage = if (editedOrdinals.isEmpty()) state.stage else CoverLetterStage.READY,
        paragraphs = state.paragraphs.map { paragraph ->
            if (paragraph.ordinal in editedOrdinals) paragraph.withEditedText(paragraph.text) else paragraph
        },
    )
}

internal fun CoverLetterParagraph.withEditedText(replacement: String): CoverLetterParagraph = copy(
    text = replacement,
    isUserEdited = true,
    flag = null,
    sentences = replacement.sentences().map { sentence -> CoverLetterSentence(text = sentence, factId = null) },
)

private data class CoverLetterBasisContext(
    val fullName: String,
    val jobTitle: String,
    val jobCompany: String,
    val facts: List<CoverLetterFactRef>,
)

private val SENTENCE_BREAK = Regex("(?<=[.!?])\\s+")

private val WHITESPACE = Regex("\\s+")

private const val TMR_TARGET_MIN_WORDS = 150

private const val TMR_TARGET_MAX_WORDS = 220

internal fun String.wordCount(): Int = trim()
    .split(WHITESPACE)
    .count { token -> token.isNotEmpty() }

internal fun isWithinWordTarget(count: Int): Boolean = count in TMR_TARGET_MIN_WORDS..TMR_TARGET_MAX_WORDS

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
            val displayId = FactDisplayIds.of(entry, profile.entries)
            entry.bullets.map { bullet ->
                CoverLetterFactRef(
                    factId = bullet.id,
                    displayId = displayId,
                    entryId = entry.id,
                    category = entry.category,
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
