package com.hirehop.core.domain.offline

internal object ContactExtractor {
    private const val NATIONAL_NUMBER_LENGTH = 10
    private const val COUNTRY_CODE = "91"
    private val email = Regex("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}")
    private val phoneCandidate = Regex("(?<![\\d])[+(]?\\d[\\d\\s\\-()]{8,16}\\d(?![\\d])")
    private val contactHint = Regex("@|linkedin|github\\.com|https?://|www\\.|\\b\\d{6}\\b", RegexOption.IGNORE_CASE)

    fun email(lines: List<String>): String = lines.firstNotNullOfOrNull { email.find(it)?.value }.orEmpty()

    fun phone(lines: List<String>): String = lines.firstNotNullOfOrNull { line ->
        phoneCandidate.findAll(line).map { it.value.trim() }.firstOrNull(::isIndianMobile)
    }.orEmpty()

    fun looksLikeContact(line: String): Boolean =
        contactHint.containsMatchIn(line) || phoneCandidate.findAll(line).any { isIndianMobile(it.value) }

    private fun isIndianMobile(candidate: String): Boolean {
        val digits = candidate.filter(Char::isDigit)
        val national = if (digits.length > NATIONAL_NUMBER_LENGTH && digits.startsWith(COUNTRY_CODE)) {
            digits.removePrefix(COUNTRY_CODE)
        } else {
            digits
        }
        return national.length == NATIONAL_NUMBER_LENGTH && national.first() in '6'..'9'
    }
}
