package com.hirehop.core.domain.coverletter

import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.MatchStatus

object CoverLetterComposer {
    const val GREETING = "Dear hiring team,"
    const val EVIDENCE_LEAD_IN = "Here is the work I can show, taken from my own record."
    const val NO_MATCHING_EVIDENCE =
        "My profile has no confirmed evidence that matches this posting, so I will not claim that it does. " +
            "I would rather talk about the work already in my record than add something I cannot show."
    const val DEFAULT_MAX_EVIDENCE = 3

    fun compose(
        candidate: CandidateProfile,
        job: JobDescription,
        analysis: JobAnalysisResult,
        maxEvidence: Int = DEFAULT_MAX_EVIDENCE,
    ): CoverLetterDraft = CoverLetterDraft(
        greeting = GREETING,
        openingParagraph = opening(job, analysis),
        evidenceParagraph = evidenceParagraph(candidate, analysis, maxEvidence),
        closingParagraph = closingParagraph(candidate),
    )

    private fun opening(job: JobDescription, analysis: JobAnalysisResult): String =
        LetterText.sentences(roleSentence(job), coverageSentence(analysis))

    private fun roleSentence(job: JobDescription): String {
        val title = LetterText.field(job.title)
        val company = LetterText.field(job.company)
        return when {
            title.isNotEmpty() && company.isNotEmpty() -> "I am applying for the $title role at $company."
            title.isNotEmpty() -> "I am applying for the $title role."
            company.isNotEmpty() -> "I am applying to a role at $company."
            else -> ""
        }
    }

    private fun coverageSentence(analysis: JobAnalysisResult): String {
        val matches = analysis.gap.matches
        if (matches.isEmpty()) return ""
        val met = matches.count { it.status == MatchStatus.MET }
        return "My confirmed background covers $met of the ${matches.size} requirements in the posting."
    }

    private fun evidenceParagraph(
        candidate: CandidateProfile,
        analysis: JobAnalysisResult,
        maxEvidence: Int,
    ): String {
        val cited = EvidencePicker.pick(candidate, analysis, maxEvidence)
        if (cited.isEmpty()) return NO_MATCHING_EVIDENCE
        return LetterText.sentences(EVIDENCE_LEAD_IN, LetterText.evidenceRun(cited))
    }

    private fun closingParagraph(candidate: CandidateProfile): String {
        val name = LetterText.field(candidate.fullName)
        val signOff = if (name.isEmpty()) "" else "Yours sincerely, $name."
        return LetterText.sentences(THANKS, signOff)
    }

    private const val THANKS = "Thank you for reading my application."
}
