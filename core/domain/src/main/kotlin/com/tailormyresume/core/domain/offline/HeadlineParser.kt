package com.tailormyresume.core.domain.offline

internal data class HeadlineMatch(val index: Int, val title: String, val company: String, val headlineSentence: String? = null)

internal object HeadlineParser {
    private val ignoreCase = setOf(RegexOption.IGNORE_CASE)
    private val hiringSentence = Regex(
        "^(.{2,50}?)\\s+is\\s+(?:hiring|looking for|seeking|recruiting)\\s+(?:for\\s+)?(?:an?\\s+|the\\s+)?(.+?)(?:\\s+(?:to|who|for|in|with|at)\\s.*)?$",
        ignoreCase,
    )
    private val hiringPrefix = Regex("^(?:we are hiring|we're hiring|hiring|job opening|opening|vacancy|walk[- ]in)\\s*[:\\-–|]?\\s*(.+)$", ignoreCase)
    private val atCompany = Regex("^(.{3,60}?)\\s+(?:at|@)\\s+(.{2,60})$", ignoreCase)
    private val separated = Regex("^(.{3,60}?)\\s+[|–—-]\\s+(.{2,60})$")
    private val roleWord = Regex(
        "\\b(engineer|developer|analyst|intern|internship|trainee|executive|associate|manager|accountant|officer|designer|specialist|consultant|assistant|representative|coordinator|scientist|architect|tester|lead|fresher|apprentice|programmer|administrator|auditor|writer|marketer)\\b",
        ignoreCase,
    )
    private const val MAX_LINE_LENGTH = 100
    private const val MAX_PLAIN_TITLE_WORDS = 8

    fun parse(lines: List<String>): HeadlineMatch? =
        scan(lines) { line, _ -> structured(line) }
            ?: scan(lines) { line, _ -> fromPlainTitle(line, requireRoleWord = true) }
            ?: scan(lines) { line, index -> fromPlainTitle(line, requireRoleWord = index != 0) }

    fun parseSentences(lines: List<String>): HeadlineMatch? =
        lines.indices.firstNotNullOfOrNull { index ->
            val line = lines[index].trim()
            if (line.length <= MAX_LINE_LENGTH) return@firstNotNullOfOrNull null
            if (JdSectionHeaders.classifyExact(line) != null) return@firstNotNullOfOrNull null
            SentenceSplitter.split(line).firstNotNullOfOrNull { sentence ->
                val candidate = sentence.trim().trimEnd('.', ':')
                if (candidate.length > MAX_LINE_LENGTH) return@firstNotNullOfOrNull null
                structured(candidate)?.let { (title, company) -> HeadlineMatch(index, title, company, headlineSentence = sentence) }
            }
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
        if (prefixed != null) return structured(prefixed.groupValues[1]) ?: (prefixed.groupValues[1] to "")
        return fromHiringSentence(line) ?: fromAtCompany(line) ?: fromSeparator(line)
    }

    private fun fromHiringSentence(line: String): Pair<String, String>? =
        hiringSentence.matchEntire(line)?.let { it.groupValues[2] to it.groupValues[1] }

    private fun fromAtCompany(line: String): Pair<String, String>? {
        val match = atCompany.matchEntire(line) ?: return null
        return if (roleWord.containsMatchIn(match.groupValues[1])) match.groupValues[1] to match.groupValues[2] else null
    }

    private fun fromSeparator(line: String): Pair<String, String>? {
        val match = separated.matchEntire(line) ?: return null
        val (first, second) = match.groupValues[1] to match.groupValues[2]
        return when {
            roleWord.containsMatchIn(first) -> first to second
            roleWord.containsMatchIn(second) -> second to first
            else -> null
        }
    }

    private fun fromPlainTitle(line: String, requireRoleWord: Boolean): Pair<String, String>? {
        val wordCount = line.split(Regex("\\s+")).size
        val plausible = wordCount <= MAX_PLAIN_TITLE_WORDS && !line.contains(':')
        val roleOk = !requireRoleWord || roleWord.containsMatchIn(line)
        return if (plausible && roleOk) line to "" else null
    }
}
