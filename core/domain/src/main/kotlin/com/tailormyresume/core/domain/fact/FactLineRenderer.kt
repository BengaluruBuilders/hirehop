package com.tailormyresume.core.domain.fact

import com.tailormyresume.core.model.ProfileEntry

object FactLineRenderer {
    private const val SEPARATOR = " · "

    fun render(draft: FactDraft): String = compose(
        title = draft.title,
        detail = draft.detail,
        organization = draft.organization,
        span = span(draft.startDate, draft.endDate),
    )

    fun render(entry: ProfileEntry): String = compose(
        title = entry.title,
        detail = entry.bullets.firstOrNull()?.text.orEmpty(),
        organization = entry.organization,
        span = span(entry.startDate, entry.endDate),
    )

    private fun compose(title: String, detail: String, organization: String, span: String): String =
        listOf(title, detail, organization, span)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(SEPARATOR)

    private fun span(startDate: String, endDate: String): String {
        val start = startDate.trim()
        val end = endDate.trim()
        return when {
            start.isNotEmpty() && end.isNotEmpty() -> "$start to $end"
            else -> start.ifEmpty { end }
        }
    }
}
