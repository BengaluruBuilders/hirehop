package com.hirehop.core.domain.coverletter

internal object LetterText {
    private val tokenMarks = charArrayOf('{', '}', '[', ']', '<', '>', '|', '$', '%')

    fun field(raw: String): String = raw.trim().filterNot(tokenMarks::contains).trim()

    fun sentences(vararg parts: String): String = parts.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")

    fun evidenceRun(texts: List<String>): String =
        texts.joinToString(separator = "; ", postfix = ".") { it.trim().trimEnd('.', ';', ' ').trim() }
}
