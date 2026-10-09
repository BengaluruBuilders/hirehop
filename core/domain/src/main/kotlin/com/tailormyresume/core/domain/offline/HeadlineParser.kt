package com.tailormyresume.core.domain.offline

internal data class HeadlineMatch(val index: Int, val title: String, val company: String, val headlineSentence: String? = null)

internal object HeadlineParser {
    private val ignoreCase = setOf(RegexOption.IGNORE_CASE)
    private val hiringSentence = Regex(
        "^(.{2,50}?)\\s+is\\s+(?:hiring|looking for|seeking|recruiting)\\s+(?:for\\s+)?(?:an?\\s+|the\\s+)?(.+?)(?:\\s+(?:to|who|for|in|with|at)\\s.*)?$",
        ignoreCase,
    )
    private val hiringPrefix = Regex(
        "^(?:\\bwe(?:\\s+are|'re)\\s+hiring|\\bnow\\s+hiring|\\bjob\\s+opening|\\bvacancy|\\bwalk[-\\s]in|" +
            "\\b(?:opening|hiring)\\s*[:\\-–|]|\\bhiring\\s+an?\\s+)\\s*(.+)$",
        ignoreCase,
    )
    private val atCompany = Regex("^(.{3,60}?)\\s+(?:at|@)\\s+(.{2,60})$", ignoreCase)
    private val separated = Regex("^(.{3,60}?)\\s+[|–—-]\\s+(.{2,60})$")
    private val roleWord = Regex(
        "\\b(engineer|developer|analyst|intern|internship|trainee|executive|associate|manager|accountant|officer|designer|specialist|consultant|assistant|representative|coordinator|scientist|architect|tester|lead|fresher|apprentice|programmer|administrator|auditor|writer|marketer)\\b",
        ignoreCase,
    )
    private val whitespace = Regex("\\s+")
    private val companyStopWords = setOf("is", "are", "was", "will", "can", "should", "must", "need", "needs", "have", "has", "plus", "required")
    private val companyMinorWords = setOf("of", "and", "&", "the", "for")
    private val rejectedCompanies =
        setOf("our team", "our client", "the team", "the ideal candidate", "we", "you", "us", "our company", "a startup", "a client")
    private const val MAX_LINE_LENGTH = 100
    private const val MAX_PLAIN_TITLE_WORDS = 8
    private const val MAX_COMPANY_WORDS = 5

    fun parse(lines: List<String>): HeadlineMatch? =
        scan(lines) { line, _ -> structured(line) }
            ?: scan(lines) { line, _ -> fromPlainTitle(line, requireRoleWord = true) }
            ?: scan(lines) { line, index -> fromPlainTitle(line, requireRoleWord = index != 0) }

    fun parseSentences(lines: List<String>): HeadlineMatch? {
        val line = lines.firstOrNull()?.trim() ?: return null
        if (line.length <= MAX_LINE_LENGTH) return null
        if (JdSectionHeaders.classifyExact(line) != null) return null
        val sentence = SentenceSplitter.split(line).firstOrNull() ?: return null
        val candidate = sentence.trim().trimEnd('.', ':')
        if (candidate.length > MAX_LINE_LENGTH) return null
        val (title, company) = structured(candidate) ?: return null
        return HeadlineMatch(0, title, company, headlineSentence = sentence)
    }

    private fun scan(lines: List<String>, parser: (String, Int) -> Pair<String, String>?): HeadlineMatch? =
        lines.indices.firstNotNullOfOrNull { index ->
            val line = lines[index].trim().trimEnd('.', ':')
            if (isUnsuitable(line)) return@firstNotNullOfOrNull null
            parser(line, index)?.let { (title, company) -> HeadlineMatch(index, title, company) }
        }

    private fun isUnsuitable(line: String): Boolean =
        line.length > MAX_LINE_LENGTH || JdSectionHeaders.classifyExact(line) != null

    private fun structured(line: String): Pair<String, String>? {
        val prefixed = hiringPrefix.matchEntire(line)
        if (prefixed != null) {
            val remainder = prefixed.groupValues[1].trim().trimStart(':', '-', '\u2013', '|').trim()
            structured(remainder)?.let { return it }
            return if (isPlainRole(remainder)) remainder to "" else null
        }
        return fromHiringSentence(line) ?: fromAtCompany(line) ?: fromSeparator(line)
    }

    private fun isPlainRole(text: String): Boolean =
        roleWord.containsMatchIn(text) && text.split(whitespace).size <= MAX_PLAIN_TITLE_WORDS

    private fun fromHiringSentence(line: String): Pair<String, String>? {
        val match = hiringSentence.matchEntire(line) ?: return null
        val title = match.groupValues[2]
        val company = match.groupValues[1]
        return if (roleWord.containsMatchIn(title) && validCompany(company)) title to company else null
    }

    private fun fromAtCompany(line: String): Pair<String, String>? {
        val match = atCompany.matchEntire(line) ?: return null
        val title = match.groupValues[1]
        val company = match.groupValues[2]
        return if (roleWord.containsMatchIn(title) && validCompany(company)) title to company else null
    }

    private fun fromSeparator(line: String): Pair<String, String>? {
        val match = separated.matchEntire(line) ?: return null
        val first = match.groupValues[1]
        val second = match.groupValues[2]
        return when {
            roleWord.containsMatchIn(first) && validCompany(second) -> first to second
            roleWord.containsMatchIn(second) && validCompany(first) -> second to first
            else -> null
        }
    }

    private fun validCompany(company: String): Boolean {
        val words = company.trim().split(whitespace).map { it.trim(',', '.', ';', ':', '(', ')') }.filter { it.isNotEmpty() }
        if (words.isEmpty() || words.size > MAX_COMPANY_WORDS) return false
        val head = words.first().first()
        if (!head.isUpperCase() && !head.isDigit()) return false
        if (words.any { it.lowercase() in companyStopWords }) return false
        if (words.any { it == it.lowercase() && it !in companyMinorWords }) return false
        return company.trim().lowercase() !in rejectedCompanies
    }

    private fun fromPlainTitle(line: String, requireRoleWord: Boolean): Pair<String, String>? {
        val wordCount = line.split(whitespace).size
        val plausible = wordCount <= MAX_PLAIN_TITLE_WORDS && !line.contains(':')
        val roleOk = !requireRoleWord || roleWord.containsMatchIn(line)
        return if (plausible && roleOk) line to "" else null
    }
}
