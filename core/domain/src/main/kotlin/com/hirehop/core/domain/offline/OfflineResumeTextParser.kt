package com.hirehop.core.domain.offline

import com.hirehop.core.domain.ResumeTextParser
import com.hirehop.core.model.CandidateProfile
import javax.inject.Inject

class OfflineResumeTextParser @Inject constructor() : ResumeTextParser {
    override suspend fun parse(rawText: String): CandidateProfile {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val nameIndex = lines.indexOfFirst { it.lowercase().trim(':') !in documentTitles }
        val session = ResumeParseSession()
        lines.filterIndexed { index, _ -> index != nameIndex }.forEach(session::consume)
        return CandidateProfile(
            fullName = lines.getOrNull(nameIndex)?.let(::nameFrom).orEmpty(),
            email = ContactExtractor.email(lines),
            phone = ContactExtractor.phone(lines),
            headline = headlineFrom(session.preambleLines),
            skills = session.collectedSkills,
            entries = session.buildEntries(),
        )
    }

    private fun nameFrom(line: String): String {
        val first = line.split(nameSeparators).first().trim().trimEnd(',')
        val looksLikeContact = first.contains('@') || first.count(Char::isDigit) >= MIN_DIGITS_FOR_CONTACT
        return if (looksLikeContact) "" else first
    }

    private fun headlineFrom(preamble: List<String>): String =
        preamble.firstOrNull(::isHeadline).orEmpty()

    private fun isHeadline(line: String): Boolean =
        !ContactExtractor.looksLikeContact(line) &&
            line.length <= MAX_HEADLINE_LENGTH &&
            !line.contains(':') &&
            line.split(" ").size >= MIN_HEADLINE_WORDS

    private companion object {
        const val MIN_DIGITS_FOR_CONTACT = 6
        const val MAX_HEADLINE_LENGTH = 80
        const val MIN_HEADLINE_WORDS = 3
        val nameSeparators = Regex("\\s*[|•·]\\s*")
        val documentTitles = setOf("resume", "curriculum vitae", "cv", "curriculum vitae (cv)", "biodata", "bio-data")
    }
}
