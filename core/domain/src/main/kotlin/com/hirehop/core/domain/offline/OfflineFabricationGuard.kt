package com.hirehop.core.domain.offline

import com.hirehop.core.domain.FabricationGuard
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.GuardrailViolation
import javax.inject.Inject

internal class OfflineFabricationGuard @Inject constructor() : FabricationGuard {
    override fun check(
        proposedText: String,
        sources: List<EvidenceBullet>,
        profile: CandidateProfile,
    ): List<GuardrailViolation> {
        if (sources.isEmpty()) return listOf(GuardrailViolation.MissingSource)
        val sourceTexts = sources.map { it.text }
        return (
            numberViolations(proposedText, sourceTexts) +
                termViolations(proposedText, sourceTexts) +
                tokenViolations(proposedText, sourceTexts) +
                verbViolations(proposedText, sourceTexts) +
                scaleViolations(proposedText, sourceTexts)
            ).distinct()
    }

    private fun numberViolations(proposed: String, sourceTexts: List<String>): List<GuardrailViolation> =
        NumberClaims.unsupported(proposed, sourceTexts).map { GuardrailViolation.UnsupportedNumber(it.raw) }

    private fun termViolations(proposed: String, sourceTexts: List<String>): List<GuardrailViolation> {
        val supported = sourceTexts.flatMap { SkillLexicon.canonicalsIn(it) }.toSet()
        return SkillLexicon.canonicalsIn(proposed)
            .filter { it !in supported }
            .map { GuardrailViolation.UnsupportedTerm(SkillLexicon.displayName(it)) }
    }

    private fun tokenViolations(proposed: String, sourceTexts: List<String>): List<GuardrailViolation> =
        TokenSubset.unsupported(proposed, sourceTexts).map { GuardrailViolation.UnsupportedTerm(it) }

    private fun verbViolations(proposed: String, sourceTexts: List<String>): List<GuardrailViolation> {
        val strongestViolation = strongestVerbViolation(proposed, sourceTexts)
        return listOfNotNull(strongestViolation ?: leadingVerbViolation(proposed, sourceTexts))
    }

    private fun strongestVerbViolation(proposed: String, sourceTexts: List<String>): GuardrailViolation? {
        val proposedVerb = OwnershipVerbs.strongest(proposed) ?: return null
        val sourceVerb = sourceTexts.mapNotNull(OwnershipVerbs::strongest).maxByOrNull { it.rank }
        return escalation(sourceVerb, proposedVerb)
    }

    private fun leadingVerbViolation(proposed: String, sourceTexts: List<String>): GuardrailViolation? {
        val proposedVerb = OwnershipVerbs.leading(proposed) ?: return null
        val sourceVerb = sourceTexts.mapNotNull(OwnershipVerbs::leading).maxByOrNull { it.rank }
        return escalation(sourceVerb, proposedVerb)
    }

    private fun escalation(source: VerbHit?, proposed: VerbHit): GuardrailViolation? {
        if (proposed.rank <= (source?.rank ?: 0)) return null
        return GuardrailViolation.VerbEscalation(from = source?.word ?: NO_VERB, to = proposed.word)
    }

    private fun scaleViolations(proposed: String, sourceTexts: List<String>): List<GuardrailViolation> =
        ScaleClaims.unsupported(proposed, sourceTexts).map { GuardrailViolation.UnsupportedScaleClaim(it) }

    private companion object {
        const val NO_VERB = "none"
    }
}
