package com.hirehop.core.domain.prep

import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.keywordsStatedIn
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementMatch

object PrepQuestionGenerator {
    const val MAX_QUESTIONS = 6

    private val kindRank = mapOf(
        PrepQuestionKind.GAP to 0,
        PrepQuestionKind.CLARIFY to 1,
        PrepQuestionKind.STRENGTH to 2,
    )

    fun generate(
        analysis: JobAnalysisResult,
        profile: CandidateProfile,
        limit: Int = MAX_QUESTIONS,
    ): List<PrepQuestion> {
        if (limit <= 0) return emptyList()
        val facts = confirmedBullets(profile)
        val entryTitles = entryTitlesByFactId(profile)
        val usedIds = mutableSetOf<String>()
        return analysis.gap.matches
            .mapIndexedNotNull { index, match -> question(match, facts, entryTitles, usedIds, index) }
            .sortedWith(compareBy({ kindRank.getValue(it.first.kind) }, { it.second }))
            .map { it.first }
            .take(limit)
    }

    private fun question(
        match: RequirementMatch,
        facts: Map<String, EvidenceBullet>,
        entryTitles: Map<String, String>,
        usedIds: MutableSet<String>,
        index: Int,
    ): Pair<PrepQuestion, Int>? {
        val text = match.requirement.text.trim()
        if (text.isEmpty()) return null
        val kind = kindOf(match.status)
        if (kind == PrepQuestionKind.GAP) return gapQuestion(text, usedIds, index) to index
        val fact = match.evidenceIds.firstOrNull { id -> statesRequirement(facts[id], match.requirement) } ?: return null
        val question = PrepQuestion(
            id = idFor(kind, text, usedIds, index),
            kind = kind,
            prompt = supportedPrompt(kind, text, entryTitles[fact].orEmpty()),
            requirementText = text,
            backingFactId = fact,
        )
        return question to index
    }

    private fun gapQuestion(text: String, usedIds: MutableSet<String>, index: Int): PrepQuestion = PrepQuestion(
        id = idFor(PrepQuestionKind.GAP, text, usedIds, index),
        kind = PrepQuestionKind.GAP,
        prompt = supportedPrompt(PrepQuestionKind.GAP, text, ""),
        requirementText = text,
        backingFactId = null,
    )

    private fun kindOf(status: MatchStatus): PrepQuestionKind = when (status) {
        MatchStatus.MET -> PrepQuestionKind.STRENGTH
        MatchStatus.PARTIAL -> PrepQuestionKind.CLARIFY
        MatchStatus.GAP -> PrepQuestionKind.GAP
    }

    private fun statesRequirement(bullet: EvidenceBullet?, requirement: JobRequirement): Boolean =
        bullet != null && requirement.keywords.isNotEmpty() && keywordsStatedIn(requirement, bullet.text).isNotEmpty()

    private fun supportedPrompt(kind: PrepQuestionKind, text: String, entryTitle: String): String {
        val subject = inSentence(text)
        return when (kind) {
            PrepQuestionKind.STRENGTH -> "Your record already covers $subject." + exampleFrom(entryTitle)
            PrepQuestionKind.CLARIFY ->
                "Your record covers part of $subject. Be ready to say what you did and what you did not."
            PrepQuestionKind.GAP ->
                "This posting asks for $subject. You have no record of it yet. " +
                    "Prepare the work you did do that comes closest, and say plainly that this part is new for you."
        }
    }

    private fun exampleFrom(entryTitle: String): String =
        if (entryTitle.isEmpty()) {
            " Be ready to give one example from your record."
        } else {
            " Be ready to give one example from $entryTitle."
        }

    private fun inSentence(text: String): String = text.first().lowercase() + text.drop(1)

    private fun idFor(kind: PrepQuestionKind, text: String, usedIds: MutableSet<String>, index: Int): String {
        val base = "prep-${kind.name.lowercase()}-${RequirementSlug.of(text)}"
        if (usedIds.add(base)) return base
        return "$base-$index"
    }

    private fun confirmedBullets(candidate: CandidateProfile): Map<String, EvidenceBullet> =
        candidate.entries
            .filter { it.isConfirmed }
            .flatMap { entry -> entry.bullets }
            .associateBy { it.id }

    private fun entryTitlesByFactId(candidate: CandidateProfile): Map<String, String> =
        candidate.entries
            .filter { it.isConfirmed }
            .flatMap { entry -> entry.bullets.map { bullet -> bullet.id to entry.title.trim() } }
            .toMap()
}
