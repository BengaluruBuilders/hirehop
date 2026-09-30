package com.hirehop.feature.tailor.impl.export

internal object ExportFileName {

    fun build(name: String, company: String, role: String): String {
        val parts = listOf(name, company, role).map(::sanitise).filter { it.isNotEmpty() }
        val base = parts.joinToString(PART_SEPARATOR).take(MAX_BASE_LENGTH).trim('_')
        return "${base.ifEmpty { FALLBACK_BASE }}.pdf"
    }

    fun sanitise(part: String): String = part
        .map { if (it.isLetterOrDigit()) it else '_' }
        .joinToString("")
        .replace(REPEATED_UNDERSCORES, "_")
        .trim('_')

    private const val PART_SEPARATOR = "_"
    private const val FALLBACK_BASE = "Resume"
    private const val MAX_BASE_LENGTH = 100
    private val REPEATED_UNDERSCORES = Regex("_+")
}
